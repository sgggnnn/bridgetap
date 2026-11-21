#version 120
uniform sampler2D image;
uniform vec4 color;
varying vec2 uv;
void main(){float coverage=texture2D(image,uv).a;gl_FragColor=vec4(color.rgb,color.a*coverage);}
