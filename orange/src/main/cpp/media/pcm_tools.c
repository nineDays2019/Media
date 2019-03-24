//
// Created by yunrui on 2019/3/24.
//

#include "pcm_tools.h"

int pcm16le_split(char *url) {

    FILE *fp = fopen(url, "rb+");
    FILE *fp1 = fopen("output_l.pcm", "wb+");
    FILE *fp2 = fopen("output_r.pcm", "wb+");

    unsigned char *sample = (unsigned char *) malloc(4);
    while (!feof(fp)) {
        fread(sample, 1, 4, fp);
        // L
        fwrite(sample, 1, 2, fp1);
        // R
        fwrite(sample + 2, 1, 2, fp2);
    }

    free(sample);
    fclose(fp);
    fclose(fp1);
    fclose(fp2);

    return 0;
}

int pcm16le_halfvolumeleft(char *url) {
    FILE *fp = fopen(url, "rb+");
    FILE *fp1 = fopen("output_half_left.pcm", "wb+");

    int cnt = 0;
    unsigned char *sample = malloc(4);

    while (!feof(fp)) {
        short *samplenum = NULL;
        fread(sample, 1, 2, fp);

        samplenum = (short *) sample;
        *samplenum = (short) (*samplenum / 2);
        // L
        fwrite(sample, 1, 2, fp1);
        // R
        fwrite(sample + 2, 1, 2, fp1);

        cnt++;
    }
    printf("Sample Cnt:%d\n", cnt);

    free(sample);
    fclose(fp);
    fclose(fp1);
    return 0;
}

int pcm16le_doublespeed(char *url) {
    FILE *fp = fopen(url, "rb+");
    FILE *fp1 = fopen("output_double_speed.pcm", "wb+");

    int cnt = 0;
    unsigned char *sample = malloc(4);
    while (!feof(fp)) {
        fread(sample, 1, 4, fp);

        if (cnt % 2 != 0) {
            // L
            fwrite(sample, 1, 2, fp1);
            // R
            fwrite(sample + 2, 1, 2, fp1);
        }
        cnt++;
    }
    printf("Sample Cnt:%d\n", cnt);

    free(sample);
    fclose(fp);
    fclose(fp1);
    return 0;
}

int pcm16le_to_pcm8(char *url) {
    FILE *fp = fopen(url, "rb+");
    FILE *fp1 = fopen("output_8.pcm", "wb+");

    int cnt = 0;
    unsigned char *sample = malloc(4);

    while (!feof(fp)) {
        short *samplenum16 = NULL;
        char samplenum8 = 0;
        unsigned char samplenum8_u = 0;
        fread(sample, 1, 4, fp);
        // (-32768 ~ 32767)
        samplenum16 = (short *) sample;
        samplenum8 = (char) ((*samplenum16) >> 8);

        // (0 ~ 255)
        samplenum8_u = (unsigned char) (samplenum8 + 128);

        // L
        fwrite(&samplenum8_u, 1, 1, fp1);

        samplenum16 = (short *) (sample + 2);
        samplenum8 = (char) ((*samplenum16) >> 8);
        samplenum8_u = (unsigned char) (samplenum8 + 128);

        //R
        fwrite(&samplenum8_u, 1, 1, fp1);
        cnt++;
    }
    printf("Sample Cnt:%d\n", cnt);
    free(sample);
    fclose(fp);
    fclose(fp1);
    return 0;
}

int pcm16le_cut_singlechannel(char *url, int start_num, int dur_num) {
    FILE *fp = fopen(url, "rb+");
    FILE *fp1 = fopen("output_cut.pcm", "wb+");
    FILE *fp_stat = fopen("output_cut.txt", "wb+");

    unsigned char *sample = malloc(2);

    int cnt = 0;
    while (!feof(fp)) {
        fread(sample, 1, 2, fp);
        if (cnt > start_num && cnt <= (start_num + dur_num)) {
            fwrite(sample, 1, 2, fp1);
            short samplenum = sample[1];
            samplenum = (short) (samplenum * 256);
            samplenum = samplenum + sample[0];

            fprintf(fp_stat, "%6d", samplenum);
            if (cnt % 10 == 0) {
                fprintf(fp_stat, "\n");
            }
        }
        cnt++;
    }

    free(sample);
    fclose(fp);
    fclose(fp1);
    fclose(fp_stat);
    return 0;
}

int pcm16le_to_wave(const char *pcmpath,
                    int channels, int sample_rate,
                    const char *wavepath) {

    typedef struct WAVE_HEADER {
        char fccID[4];
        unsigned long dwSize;
        char fccType[4];
    } WAVE_HEADER;

    typedef struct WAVE_FMT {
        char fccID[4];
        unsigned long dwSize;
        unsigned short wFormatTag;
        unsigned short wChannels;
        unsigned long dwSamplesPerSec;
        unsigned long dwAvgBytesPerSec;
        unsigned short wBlockAlign;
        unsigned short uiBitsPerSample;
    } WAVE_FMT;

    typedef struct WAVE_DATA {
        char fccID[4];
        unsigned long dwSize;
    } WAVE_DATA;

    if (channels == 0 || sample_rate == 0) {
        channels = 2;
        sample_rate = 44100;
    }

    int bits = 16;

    WAVE_HEADER pcmHEADER;
    WAVE_FMT pcmFMT;
    WAVE_DATA pcmDATA;

    unsigned short m_pcmData;
    FILE *fp, *fpout;

    fp = fopen(pcmpath, "rb+");
    fpout = fopen(wavepath, "wb+");

    // WAVE_HEADER
    memcpy(pcmHEADER.fccID, "RIFF", strlen("RIFF"));
    memcpy(pcmHEADER.fccType, "WAVE", strlen("WAVE"));
    fseek(fpout, sizeof(WAVE_HEADER), 1);

    // WAVE_FMT
    pcmFMT.dwSamplesPerSec = (unsigned long) sample_rate;
    pcmFMT.dwAvgBytesPerSec = pcmFMT.dwSamplesPerSec * sizeof(m_pcmData);
    pcmFMT.uiBitsPerSample = (unsigned short) bits;
    memcpy(pcmFMT.fccID, "fmt ", strlen("fmt "));
    pcmFMT.dwSize = 16;
    pcmFMT.wBlockAlign = 2;
    pcmFMT.wChannels = (unsigned short) channels;
    pcmFMT.wFormatTag = 1;

    fwrite(&pcmFMT, sizeof(WAVE_FMT), 1, fpout);

    // WAVE_DATA
    memcpy(pcmDATA.fccID, "data", strlen("data"));
    pcmDATA.dwSize = 0;
    fseek(fpout, sizeof(WAVE_DATA), SEEK_CUR);

    fread(&m_pcmData, sizeof(unsigned short), 1, fp);
    while (!feof(fp)) {
        pcmDATA.dwSize += 2;
        fwrite(&m_pcmData, sizeof(unsigned short), 1, fpout);
        fread(&m_pcmData, sizeof(unsigned short), 1, fp);
    }

    pcmHEADER.dwSize = 44 + pcmDATA.dwSize;

    rewind(fpout);
    fwrite(&pcmHEADER, sizeof(WAVE_HEADER), 1, fpout);
    fseek(fpout, sizeof(WAVE_FMT), SEEK_CUR);
    fwrite(&pcmDATA, sizeof(WAVE_DATA), 1, fpout);

    fclose(fp);
    fclose(fpout);

    return 0;
}