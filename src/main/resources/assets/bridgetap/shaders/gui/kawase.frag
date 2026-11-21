#version 120
uniform sampler2D image;
uniform vec2 texel;
uniform float offset;
varying vec2 uv;
// Separable Gaussian: axial bilinear taps avoid diagonal star-shaped highlights.
void main(){
 vec2 d=texel*offset;
 vec4 sum=texture2D(image,uv)*0.2270270270;
 sum+=(texture2D(image,uv+d*1.3846153846)+texture2D(image,uv-d*1.3846153846))*0.3162162162;
 sum+=(texture2D(image,uv+d*3.2307692308)+texture2D(image,uv-d*3.2307692308))*0.0702702703;
 gl_FragColor=sum;
}
