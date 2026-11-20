package com.likelion.backend.global.init;

import java.util.List;

/**
 * 사전 입양 가이드 단계별 기준 콘텐츠 (크레스티드 게코).
 * 디자인 시안(데모데이) 문구 기준.
 */
final class PreAdoptionGuideSeedData {

    private PreAdoptionGuideSeedData() {
    }

    static final String SPECIES_NAME = "크레스티드 게코";
    static final String GUIDE_TITLE = "크레스티드 게코 입양 준비";
    static final String GUIDE_DESCRIPTION = "입양 전 사육 환경을 준비해요.";

    record Content(String title, String badge, String text) {
        static Content of(String text) {
            return new Content(null, null, text);
        }
    }

    record Block(String title, List<String> items) {
    }

    record Detail(String question, List<Content> contents, List<Block> blocks) {
    }

    record Section(String title, String content, List<Detail> details) {
    }

    record CheckItem(String group, String title, String description) {
        static CheckItem of(String title) {
            return new CheckItem(null, title, null);
        }
    }

    record Step(int number, String title, String description, List<Section> sections, List<CheckItem> checkItems) {
    }

    static final List<Step> STEPS = List.of(
            new Step(
                    1,
                    "사육장 준비",
                    "도마뱀에게 맞는 사육장의 크기와 소재를 확인해요.",
                    List.of(
                            new Section(
                                    "사육장 형태",
                                    "높이가 충분한 세로형이 좋아요. 크레스티드 게코는 나무를 타고 생활하는 수목성 도마뱀이에요. 바닥 면적뿐 아니라 위아래로 이동할 수 있는 높이를 충분히 확보해주세요.",
                                    List.of(new Detail(
                                            "왜 세로형이 필요한가요?",
                                            List.of(
                                                    Content.of("크레스티드 게코는 가지와 식생을 오르내리며 생활하기 때문에 수직 공간을 적극적으로 사용해요."),
                                                    Content.of("그래서 낮고 넓은 형태보다 높이가 확보된 사육장에 덩굴, 은신 공간을 여러 높이로 배치할 수 있는 구조가 적합해요.")
                                            ),
                                            List.of()
                                    ))
                            ),
                            new Section(
                                    "사육장 크기",
                                    "성장 단계에 맞는 공간을 준비해주세요. 성체 한 마리라면 우선 45 × 45 × 60cm 이상을 기준으로 확인해주세요.",
                                    List.of()
                            ),
                            new Section(
                                    "소재와 환기",
                                    "습도 유지와 환기를 함께 확인해주세요. 유리·PVC·아크릴 등 소재만 보기보다 습도를 유지하면서 공기가 통할 수 있는 구조인지 확인해주세요.",
                                    List.of(new Detail(
                                            "소재는 무엇을 골라야 하나요?",
                                            List.of(
                                                    Content.of("특정 소재 하나를 ‘정답’으로 지정하기보다는, 환기구와 메쉬 구조가 있는지, 습도 관리와 청소가 가능한지를 함께 확인해야 해요.")
                                            ),
                                            List.of(new Block(
                                                    "기관 권고 사항",
                                                    List.of(
                                                            "RSPCA는 높은 습도 환경 때문에 목재보다 유리 사육장을 선호한다고 안내해요.",
                                                            "PetMD는 적절한 환기가 가능한 스크린 구조를 강조해요."
                                                    )
                                            ))
                                    ))
                            ),
                            new Section(
                                    "설치 위치",
                                    "온도가 크게 흔들리지 않는 곳에 놓아주세요. 직사광선, 난방기, 에어컨 바람이 직접 닿는 위치는 피해주세요.",
                                    List.of()
                            )
                    ),
                    List.of()
            ),
            new Step(
                    2,
                    "내부 환경 세팅",
                    "도마뱀에게 필요한 조명과 은신처, 바닥재 등을 준비해요.",
                    List.of(
                            new Section(
                                    "이동/은신 공간",
                                    "오르고 숨을 수 있는 공간을 만들어주세요. 가지와 덩굴을 여러 높이로 연결하고, 몸을 가리고 쉴 수 있는 공간도 함께 마련해주세요.",
                                    List.of(new Detail(
                                            "어떻게 배치하면 좋을까요?",
                                            List.of(
                                                    Content.of("가지와 덩굴을 단순히 장식처럼 두기보다, 사육장 아래에서 위쪽까지 이동할 수 있도록 서로 이어지게 배치해주세요.")
                                            ),
                                            List.of(new Block(
                                                    "이렇게 배치해보세요",
                                                    List.of(
                                                            "높이가 다른 가지를 세로·가로·대각선 방향으로 섞어 한쪽 구조물에서 다른 구조물로 이동할 수 있게 해주세요.",
                                                            "높은 위치에도 머물거나 이동할 수 있는 가지와 덩굴을 마련해주세요.",
                                                            "몸을 가릴 공간을 함께 만들어요. 코르크 바크, 코코넛 은신처, 넓은 잎 등을 이용해 낮 동안 몸을 숨기고 쉴 수 있는 장소를 만들어주세요.",
                                                            "구조물이 흔들리지 않는지 확인해요. 점프했을 때 쉽게 떨어지거나 넘어지는 장식은 피해주세요."
                                                    )
                                            ))
                                    ))
                            ),
                            new Section(
                                    "바닥재",
                                    "성장 단계와 관리 방식에 맞는 바닥재를 준비해주세요. 습도 관리와 청소가 가능하고 개체에게 안전한 바닥재인지 확인해주세요.",
                                    List.of(new Detail(
                                            "어떤 바닥재가 좋을까요?",
                                            List.of(
                                                    Content.of("바닥재는 습도 유지뿐 아니라 청소와 관찰에도 영향을 줘요."),
                                                    Content.of("크레스티드 게코 사육에서는 코코넛 파이버나 토양계 바닥재 등이 사용되지만, 어린 개체나 상태를 자주 관찰해야 하는 경우에는 관리하기 쉬운 종이타월을 사용해요.")
                                            ),
                                            List.of(new Block(
                                                    "이런 바닥재는 피해주세요",
                                                    List.of(
                                                            "나무 조각이나 우드칩처럼 삼켰을 때 문제가 될 수 있는 큰 입자의 바닥재는 피하는 편이 좋아요."
                                                    )
                                            ))
                                    ))
                            ),
                            new Section(
                                    "물/먹이 공간",
                                    "편하게 접근할 수 있는 급여 공간을 마련해주세요. 깨끗한 물을 제공하고 먹이와 물그릇이 안정적으로 놓여 있는지 확인해주세요.",
                                    List.of()
                            ),
                            new Section(
                                    "조명/히팅 장비",
                                    "빛과 온도를 관리할 장비를 준비해주세요. 낮과 밤의 주기를 위한 조명과 필요한 열원을 준비하고, 열원을 사용할 경우 온도조절기를 함께 설치해주세요.",
                                    List.of(new Detail(
                                            "어떤 장비가 필요한가요?",
                                            List.of(
                                                    new Content(
                                                            "주간 조명",
                                                            null,
                                                            "낮과 밤을 구분할 수 있도록 일정한 주기로 빛을 제공해주세요. 밤에는 가시광 조명을 끄는 것이 좋아요. 타이머를 사용하면 매일 비슷한 시간에 켜고 끌 수 있어요."
                                                    ),
                                                    new Content(
                                                            "히팅 장비",
                                                            "필요한 경우",
                                                            "실내 온도만으로 필요한 온도 환경을 유지하기 어렵다면 열원을 추가할 수 있어요. 열 램프나 세라믹 히터 등의 선택지는 있지만 사육장의 실제 측정 온도를 먼저 확인한 뒤 결정해주세요."
                                                    ),
                                                    new Content(
                                                            "온도조절기",
                                                            "열원 사용 시 필수",
                                                            "열원을 사용한다면 온도조절기(thermostat)를 함께 연결해주세요. 설정 온도를 넘어 계속 가열되는 상황을 방지하는 역할을 해요."
                                                    )
                                            ),
                                            List.of(new Block(
                                                    "설치 전 꼭 확인해주세요",
                                                    List.of(
                                                            "열원은 도마뱀이 직접 닿아 화상을 입지 않도록 설치해야 해요. 열 램프는 사육장 외부 또는 적절한 가드와 함께 사용하고, 핫락(가열 돌)은 사용하지 않는 것이 좋아요."
                                                    )
                                            ))
                                    ))
                            )
                    ),
                    List.of(
                            new CheckItem(null, "가지와 덩굴을 여러 높이에 연결해 설치했어요", "위아래로 이동할 수 있는 경로가 있어요."),
                            new CheckItem(null, "몸을 충분히 가릴 수 있는 은신 공간을 마련했어요", "잎이나 은신처처럼 편하게 숨어 쉴 공간이 있어요."),
                            new CheckItem(null, "성장 단계와 관리 방식에 맞는 바닥재를 준비했어요", "바닥재가 지나치게 젖어 있거나 오염되어 있지 않아요."),
                            new CheckItem(null, "물과 먹이에 편하게 접근할 수 있는 공간을 마련했어요", "그릇이 쉽게 넘어지지 않도록 안정적으로 배치했어요."),
                            new CheckItem(null, "낮과 밤을 구분할 수 있는 조명을 준비했어요", "일정한 주기로 조명을 켜고 끌 수 있어요.")
                    )
            ),
            new Step(
                    3,
                    "센서 설치",
                    "사육장에 센서를 설치하고 온습도가 잘 측정되는지 확인해요.",
                    List.of(
                            new Section(
                                    "센서 연결하기",
                                    "핸드폰의 블루투스를 통해 온습도 센서를 앱과 연결해주세요.",
                                    List.of()
                            ),
                            new Section(
                                    "센서 설치하기",
                                    "도마뱀이 주로 생활하는 환경을 확인할 수 있는 위치에 설치해주세요.\n\n센서 위치에 따라 측정값이 달라질 수 있으므로, 열원이나 분무가 직접 닿는 곳처럼 특정 영향을 지나치게 크게 받는 위치는 피해주세요.",
                                    List.of()
                            )
                    ),
                    List.of(
                            CheckItem.of("센서가 열원 바로 아래에 있지 않아요"),
                            CheckItem.of("분무 시 물이 센서에 직접 닿지 않아요"),
                            CheckItem.of("물그릇 바로 옆처럼 습도가 국소적으로 높은 곳을 피했어요"),
                            CheckItem.of("도마뱀이 센서를 떨어뜨리거나 손상시키지 않도록 고정했어요")
                    )
            ),
            new Step(
                    4,
                    "환경 사전 점검",
                    "센서 데이터를 통해 사육장의 온습도 변화를 확인해요.",
                    List.of(
                            new Section(
                                    "온습도 체크 중",
                                    "도마뱀을 데려오기 전, 센서로 온·습도 변화를 7일 동안 확인해요. 실제로 사육할 때처럼 조명과 분무 등을 관리해주세요.",
                                    List.of()
                            )
                    ),
                    List.of()
            ),
            new Step(
                    5,
                    "준비 상태 확인",
                    "도마뱀을 데려오기 전 사육환경이 잘 준비되었는지 최종 확인해요.",
                    List.of(),
                    List.of(
                            new CheckItem("사육장", "문과 잠금장치가 잘 닫혀 있어요", null),
                            new CheckItem("사육장", "가지와 은신처가 흔들리지 않게 고정되어 있어요", null),
                            new CheckItem("생활 준비", "깨끗한 물과 물그릇을 준비했어요", null),
                            new CheckItem("생활 준비", "먹이와 급여 방법을 미리 확인했어요", null),
                            new CheckItem("생활 준비", "입양 직후 편하게 숨을 수 있는 공간을 마련했어요", null),
                            new CheckItem("환경 및 센서", "센서가 정상적으로 연결되어 있어요", null),
                            new CheckItem("환경 및 센서", "센서가 처음 설치한 위치에 잘 고정되어 있어요", null),
                            new CheckItem("환경 및 센서", "7일간 온·습도 변화 확인을 완료했어요", null)
                    )
            )
    );
}
