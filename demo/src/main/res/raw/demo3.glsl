#ifdef GL_ES
precision mediump float;
#endif

#define PI 3.14159265359
#define PROCESSING_COLOR_SHADER

uniform vec2 u_resolution; // 宽高
uniform vec3 u_mouse;
uniform float u_time;

// 绘制一条直线，宽度为 0.04
float plot(vec2 st, float pct){
  return  smoothstep(pct - 0.02, pct, st.y) -
          smoothstep(pct, pct + 0.02, st.y);
}

void main() {
    vec2 st = gl_FragCoord.xy / u_resolution;

    // float y = sin(PI * st.x);
    // float y = step(0.5, st.x);
    // float y = smoothstep(0.1, 0.9, st.x);
//    float y = 0.5 * sin(4.0 * PI * (u_time + st.x)) + 0.5;

//    vec3 color = vec3(y);
//    float pct = plot(st, y);
//    color = (1.0 - pct) * color + pct * vec3(0.0, 1.0, 0.0);    // 原理不明白！阿西

    gl_FragColor = vec4(0.5f,st.y,st.y, st.y);
}
// step() 插值函数需要输入两个参数，第一个是极限或阀值，第二个是想要检测或者通过的值
// 对任何小于阀值的值，返回 0.0；大于阀值，则返回 1.0

// smoothstep()
// 给定一个范围的上下限和一个数值，这个函数会在已有的范围给出插值。
// 前两个参数规定转换的开始和结束点，第三个是给出一个值用来插值。

// 其他一些有用的函数
//y = mod(x,0.5); // 返回 x 对 0.5 取模的值
//y = fract(x); // 仅仅返回数的小数部分
//y = ceil(x);  // 向正无穷取整
//y = floor(x); // 向负无穷取整
//y = sign(x);  // 提取 x 的正负号
//y = abs(x);   // 返回 x 的绝对值
//y = clamp(x,0.0,1.0); // 把 x 的值限制在 0.0 到 1.0
//y = min(0.0,x);   // 返回 x 和 0.0 中的较小值
//y = max(0.0,x);   // 返回 x 和 0.0 中的较大值
