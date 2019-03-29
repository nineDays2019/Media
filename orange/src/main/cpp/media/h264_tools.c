#include "../common/common_tools.h"

// H.264码流解析程序
// 可以从 H.264 码流中分析得到它的基本单元 NALU，并且可以简单解析 NALU 首部的字段

// H.264 原始码流（又称为“裸流”）是由一个一个的 NALU 组成的。
// 每个 NALU 之间通过 startcode（起始码）进行分割，起始码分成两种： 0x000001（3Byte）或者0x00000001（4Byte）。
// 如果 NALU 对应的 Slice 为一帧的开始就用 0x00000001，否则就用 0x000001

// H.264 码流解析的步骤就是首先从码流中搜索 0x000001 和 0x00000001，分离出 NALU；然后再分析 NALU 的各个字段。

typedef enum {
    NALU_TYPE_SLICE = 1,
    NALU_TYPE_DPA = 2,
    NALU_TYPE_DPB = 3,
    NALU_TYPE_DPC = 4,
    NALU_TYPE_IDR = 5,
    NALU_TYPE_SEI = 6,
    NALU_TYPE_SPS = 7,
    NALU_TYPE_PPS = 8,
    NALU_TYPE_AUD = 9,
    NALU_TYPE_EOSEQ = 10,
    NALU_TYPE_EOSTREAM = 11,
    NALU_TYPE_FILL = 12,
} NaluType;

typedef enum {
    NALU_PRIORITY_DISPOSABLE = 0,
    NALU_PRIRITY_LOW = 1,
    NALU_PRIORITY_HIGH = 2,
    NALU_PRIORITY_HIGHEST = 3
} NaluPriority;

typedef struct {
    int startcodeprefix_len; //! 4 for parameter sets and first slice in picture, 3 for everything else (suggested)
    unsigned len;            //! Length of the NAL unit (Excluding the start code, which does not belong to the NALU)
    unsigned max_size;       //! Nal Unit Buffer size
    int forbidden_bit;       //! should be always FALSE
    int nal_reference_idc;   //! NALU_PRIORITY_xxxx
    int nal_unit_type;       //! NALU_TYPE_xxxx
    char *buf;               //! contains the first byte followed by the EBSP
} NALU_t;

FILE *h264bitstream = NULL; // the bit stream file

int info2 = 0, info3 = 0;

static int FindStartCode2(unsigned char *Buf) {
    if (Buf[0] != 0 || Buf[1] != 0 || Buf[2] != 1)
        return 0; // 0x000001?
    else
        return 1;
}

static int FindStartCode3(unsigned char *Buf) {
    if (Buf[0] != 0 || Buf[1] != 0 || Buf[2] != 0 || Buf[3] != 1)
        return 0; // 0x00000001?
    else
        return 1;
}

int GetAnnexbNALU(NALU_t *nalu) {
    int pos = 0;
    int StartCodeFound, rewind;
    unsigned char *Buf = NULL;
    // calloc：在内存的动态存储区中分配 n 个长度为 size 的连续空间，函数返回一个指向分配起始地址的指针；
    // 如果分配不成功，返回 NULL
    // 与 malloc 的区别：
    // calloc 在动态分配完内存后，自动初始化该内存空间为零，而 malloc 不初始化，里面的数据是随机的垃圾数据
    if ((Buf == (unsigned char *) calloc(nalu->max_size, sizeof(char))) == NULL)
        LOGE("GetAnnexbNALU: Could not allocate Buf memory.\n");

    nalu->startcodeprefix_len = 3;

    if (3 != fread(Buf, 1, 3, h264bitstream)) {
        free(Buf);
        return 0;
    }

    info2 = FindStartCode2(Buf);
    if (info2 != 1) {
        if (1 != fread(Buf + 3, 1, 1, h264bitstream)) {
            free(Buf);
            return 0;
        }
        info3 = FindStartCode2(Buf);
        if (info3 != 1) {
            free(Buf);
            return -1;
        } else {
            pos = 4;
            nalu->startcodeprefix_len = 4;
        }
    } else {
        nalu->startcodeprefix_len = 3;
        pos = 3;
    }
    StartCodeFound = 0;
    info2 = 0;
    info3 = 0;

    while (!StartCodeFound) {
        if (feof(h264bitstream)) {
            nalu->len = (unsigned int) ((pos - 1) - nalu->startcodeprefix_len);
            memcpy(nalu->buf, &Buf[nalu->startcodeprefix_len], nalu->len);
            nalu->forbidden_bit = nalu->buf[0] & 0x80;  // 1 bit
            nalu->nal_reference_idc = nalu->buf[0] & 0x60;  // 2 bit
            nalu->nal_unit_type = (nalu->buf[0]) & 0x1f;    // 5 bit
            free(Buf);
            return pos - 1;
        }
        Buf[pos++] = (unsigned char) fgetc(h264bitstream);
        info3 = FindStartCode3(&Buf[pos - 4]);
        if (info3 != 1) {
            info2 = FindStartCode2(&Buf[pos - 3]);
        }
        StartCodeFound = (info2 == 1 || info3 == 1);
    }

    rewind = (info3 == 1) ? -4 : -3;

    if (0 != fseek(h264bitstream, rewind, SEEK_CUR)) {
        free(Buf);
        printf("GetAnnexbNALU: Can not fseek in the bit stream file.");
    }
    nalu->len = (unsigned int) ((pos + rewind) - nalu->startcodeprefix_len);
    memcpy(nalu->buf, &Buf[nalu->startcodeprefix_len], nalu->len);
    nalu->forbidden_bit = nalu->buf[0] & 0x80;  // 1 bit
    nalu->nal_reference_idc = nalu->buf[0] & 0x60;  // 2 bit
    nalu->nal_unit_type = (nalu->buf[0]) & 0x1f;    // 5 bit
    free(Buf);

    return (pos + rewind);
}

int h264_parser(char *url) {
    NALU_t *n;
    int buffersize = 100000;
//    FILE *out = stdout;
    FILE *out = fopen("output_log.txt", "wb+");

    h264bitstream = fopen(url, "rb+");
    if (h264bitstream == NULL) {
        LOGE("Open file error.\n");
        return 0;
    }

    n = calloc(1, sizeof(NALU_t));
    if (n == NULL) {
        LOGE("Alloc NALU Error.\n");
        return 0;
    }

    n->max_size = (unsigned int) buffersize;
    n->buf = calloc((size_t) buffersize, sizeof(char));
    if (n->buf == NULL) {
        free(n);
        LOGE("AllocNALU: n->buf");
        return 0;
    }
    int data_offset = 0;
    int nal_num = 0;
    printf("-----+-------- NALU Table ------+---------+\n");
    printf(" NUM |    POS  |    IDC |  TYPE |   LEN   |\n");
    printf("-----+---------+--------+-------+---------+\n");
    while (!feof(h264bitstream)) {
        int data_lenth;
        data_lenth = GetAnnexbNALU(n);

        char type_str[20] = {0};
        switch (n->nal_unit_type) {
            case NALU_TYPE_SLICE:
                sprintf(type_str, "SLICE");
                break;
            case NALU_TYPE_DPA:
                sprintf(type_str, "DPA");
                break;
            case NALU_TYPE_DPB:
                sprintf(type_str, "DPB");
                break;
            case NALU_TYPE_DPC:
                sprintf(type_str, "DPC");
                break;
            case NALU_TYPE_IDR:
                sprintf(type_str, "IDR");
                break;
            case NALU_TYPE_SEI:
                sprintf(type_str, "SEI");
                break;
            case NALU_TYPE_SPS:
                sprintf(type_str, "SPS");
                break;
            case NALU_TYPE_PPS:
                sprintf(type_str, "PPS");
                break;
            case NALU_TYPE_AUD:
                sprintf(type_str, "AUD");
                break;
            case NALU_TYPE_EOSEQ:
                sprintf(type_str, "EOSEQ");
                break;
            case NALU_TYPE_EOSTREAM:
                sprintf(type_str, "EOSTREAM");
                break;
            case NALU_TYPE_FILL:
                sprintf(type_str, "FILL");
                break;
        }
        char idc_str[20] = {0};
        switch (n->nal_reference_idc >> 5) {
            case NALU_PRIORITY_DISPOSABLE:
                sprintf(idc_str, "DISPOS");
                break;
            case NALU_PRIRITY_LOW:
                sprintf(idc_str, "LOW");
                break;
            case NALU_PRIORITY_HIGH:
                sprintf(idc_str, "HIGH");
                break;
            case NALU_PRIORITY_HIGHEST:
                sprintf(idc_str, "HIGHEST");
                break;
        }

        fprintf(out, "%5d| %8d| %7s| %6s| %8d|\n", nal_num, data_offset, idc_str, type_str, n->len);

        data_offset = data_offset + data_lenth;

        nal_num++;
    }
    //Free
    if (n) {
        if (n->buf) {
            free(n->buf);
            n->buf = NULL;
        }
        free(n);
    }
}