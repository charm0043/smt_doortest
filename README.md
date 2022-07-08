# smt_doortest

[project] 얼굴·모션인식 스마트 도어락 

## Description
초음파 센서를 이용하여 일정 거리 안에 사람이 감지되면 카메라가 켜지고 얼굴인식을 진행한다. 인가자임을 확인하면 모션인식을 진행하고 눈깜빡임(윙크)을 감지하면 문을 열어준다. 문이 열렸다는 알림을 휴대폰으로 보내준다. 비인가자라면 라즈베리파이 카메라로 사진을 찍어 어플로 넘겨주고 비정상적인 활동을 감지했다는 경고 알림을 보내준다.

##  Results
[Hardware]

![완성](https://user-images.githubusercontent.com/59986061/177974569-3172ccd3-016e-4114-b099-df6bfd7e3c3b.png)

[Hardware 동작 흐름도]

<img width="360" alt="동작도" src="https://user-images.githubusercontent.com/59986061/177970917-b795bb5a-3d7b-4c64-aeff-383993d47674.png">

[Software]

[Software 동작 흐름도]

<img width="221" alt="어플 출입 동작" src="https://user-images.githubusercontent.com/59986061/177974612-cd48784a-ad1b-4d3d-a281-05c4109586b3.png">

##  Environment

**1) 프로그래밍 개발 환경**

(1) PC 환경:
Window 10 Intel(R) Core(TM) i5-8265U CPU @ 1.60GHz 1.80 GHz

(2) 모바일 환경
테스트: 갤럭시7 쿼드코어 1.5Ghz, Ram 2GB, OS: Android 4.1.1

**2) 프로그래밍 개발 도구**

(1) H/W 도구:
Raspberry Pi 4, 초음파 거리 센서, 카메라 모듈, 1채널 릴레이 모듈, 솔레노이드 
잠금장치, SD카드, OpenCV

(2) S/W 도구:
Android Studio

**3) 프로그래밍 개발 언어**

python, java
