package com.likelion.backend.domain.enclosure.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 환경 코치 규칙. 문장은 상태 안내 + 확인/행동 제안까지만 하고,
 * 질병·건강 상태나 직접적인 원인은 단정하지 않는다.
 */
@Getter
@RequiredArgsConstructor
public enum CoachRule {

    HIGH_TEMPERATURE_CONTINUOUS(
            "최근 온도가 권장 범위보다 높게 유지되고 있어요.",
            "열원(히팅램프·매트) 주변과 쿨존 온도를 확인해 주세요.",
            "열원 출력이나 위치를 조절하고 통풍을 확보해 주세요."),
    LOW_TEMPERATURE_CONTINUOUS(
            "최근 온도가 권장 범위보다 낮게 유지되고 있어요.",
            "열원이 정상적으로 작동하고 있는지 확인해 주세요.",
            "열원 작동 시간과 사육장 보온 상태를 점검해 주세요."),
    HIGH_HUMIDITY_CONTINUOUS(
            "최근 습도가 권장 범위보다 높게 유지되고 있어요.",
            "사육장 내부의 환기 상태를 확인해 주세요.",
            "환기 상태를 점검하고 분무량을 조절해 주세요."),
    LOW_HUMIDITY_CONTINUOUS(
            "최근 습도가 권장 범위보다 낮게 유지되고 있어요.",
            "바닥재와 은신처의 수분 상태를 확인해 주세요.",
            "바닥재를 직접 만져 확인한 뒤 필요할 때 가볍게 분무해 주세요."),

    HIGH_TEMPERATURE_RECENT(
            "최근 측정에서 온도가 권장 범위보다 조금 높아졌어요.",
            "열원 주변 온도를 한 번 확인해 주세요.",
            "일시적인 변화일 수 있으니 잠시 후 온도를 다시 확인해 주세요."),
    LOW_TEMPERATURE_RECENT(
            "최근 측정에서 온도가 권장 범위보다 조금 낮아졌어요.",
            "열원이 켜져 있는지 확인해 주세요.",
            "일시적인 변화일 수 있으니 잠시 후 온도를 다시 확인해 주세요."),
    HIGH_HUMIDITY_RECENT(
            "최근 측정에서 습도가 권장 범위보다 조금 높아졌어요.",
            "분무 직후라면 자연스러운 변화일 수 있어요.",
            "잠시 후 습도가 다시 내려가는지 확인해 주세요."),
    LOW_HUMIDITY_RECENT(
            "최근 측정에서 습도가 권장 범위보다 조금 낮아졌어요.",
            "바닥재가 많이 말라 있는지 확인해 주세요.",
            "잠시 후 습도 변화를 다시 확인해 주세요."),

    STABLE(
            "온도와 습도가 권장 범위 안에서 안정적으로 유지되고 있어요.",
            "평소처럼 물그릇과 바닥재 상태를 확인해 주세요.",
            "지금처럼 관리를 유지해 주세요.");

    private final String currentSituation;
    private final String checkItem;
    private final String recommendedAction;
}
