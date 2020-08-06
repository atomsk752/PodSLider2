# PodSLider2

본 프로젝트는 안드로이드의 탭레이아웃의 그래픽을 개선하는 라이브러리이며 기본적으로 [PodSlider](https://github.com/bhargavms/PodSLider)의 파생형입니다.  
기존의 PodSlider 라이브러리는 4년간 커밋이 없고 원제작자 계정도 활동이 없는 상태이며  
해당 라이브러리가 소개된 내용과 다르게 동작하여 이를 개선하고 지속적으로 유지보수하기 위해 새 프로젝트를 만들었습니다.  
대부분의 코드가 원버전과 같기 때문에 [Apache-2.0](http://www.apache.org/licenses/LICENSE-2.0.txt) 라이센스를 똑같이 유지합니다.

## 동작화면

![Sample Gif](ezgif.com-gif-maker.gif?raw=true)
![Sample Gif](view_pager_interaction.gif?raw=true)

## 사용법

build.gradle 파일에 다음의 내용 추가
```gradle
dependencies {
  implementation 'com.honeyrock:PodSLider2:1.0.1'
}
```


#### Xml 속성
```xml
app:mainSliderColor="@color/mainPodSlider" <!-- the color of the main rounded rectangular bar. -->
app:numberOfPods="2" <!-- the number of small circles (i.e pods) in the slider.-->
app:podColor="#4CAF50" <!-- the color of the pod when its not selected.-->
app:selectedPodColor="#fff" <!-- the color of the pod when its selected.-->
```
#### Setting a click listener
```java
PodSlider podSlider = (PodSlider) findViewById(R.id.pod_slider);
podSlider.setPodClickListener(new OnPodClickListener() {
    @Override
    public void onPodClick(int position) {
        Log.d("PodPosition", "position = " + position);
    }
});
```

#### To set Currently selected Pod
```java
podSlider.setCurrentlySelectedPod(1);
// 본 기능은 뷰가 렌더링 된 다음에 동작해야합니다.
// onCreate()/onCreateView()에 사용시 오류가 발생할 수 있습니다.
// this will cause the large and medium circle to be drawn at (0, height/2)
podSlider.setcurrentlySelectedPodAndAnimate(1);
```

#### To set up with a ViewPager
> By default, the widget uses the String returned by the viewpager 
> adapter's `getPageTitle(int)` method, to disable it just set
> `pager.usePageTitle(false)` as false, **before setting up with viewPager** 

```java
ViewPager pager = (ViewPager) findViewById(R.id.pager);
PodSlider pagerSlider = (PodSlider) findViewById(R.id.pager_slider);
PodPagerAdapter adapter = new PodPagerAdapter(getSupportFragmentManager());
pager.setAdapter(adapter);
pagerSlider.setUpWithViewPager(pager);
```

#### 코드 상에서 pod갯수 설정하기
```java
podSlider.setNumberOfPods(4);
```

#### To set custom text 

```
// Use this after setting number of pods and make sure the length of texts 
// array is equal to or greater than `numberOfPods` that you set using `setNumberOfPods()`
podSlider.setPodTexts(new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"});
```

> (한 개 글자만 사용하길 권장합니다)

#### To set Drawables

```
Drawable access = getResources().getDrawable(R.drawable.ic_accessibility_black_24dp);
Drawable account = getResources().getDrawable(R.drawable.ic_account_circle_black_24dp);
Drawable car = getResources().getDrawable(R.drawable.ic_directions_car_black_24dp);
// Use this after setting number of pods and make sure the length 
// of drawables array is equal to or greater than `numberOfPods`
// that you set using `setNumberOfPods()`
podSlider.setPodDrawables(
        new Drawable[]{
                access, account, car,
                access, account, car,
                access, account, car,
                access
        }, PodSlider.DrawableSize.FIT_POD_CIRCLE
);
```


 
 ## 1.0.2추가예정 기능
 Pod 그림자 on/off 기능
 
