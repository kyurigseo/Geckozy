import { TANK_PARTS } from './tankParts'

import wallCream from '../../assets/onboarding/tank-wall-cream.png'

import './TankPreview.scss'

// 사육장 틀 = 크림 벽지 이미지(562x560) 자리. 커스텀 화면 무대(393x431) 기준 px
// 모든 레이어가 이 틀 안에 들어가서, 미리보기 크기는 이 틀 하나로 정한다 (TankPreview.scss의 aspect-ratio)
const FRAME = { left: 55, top: 159, width: 280.5, height: (280.5 * 560) / 562 }

// 사육장 본체: 벽지·빈 사육장 이미지가 각각 따로 그려져 안쪽 모양이 조금씩 달라서,
// 기준인 크림 벽지 이미지 하나를 쓰고 벽면 색만 바꾼다 (TankPreview.scss의 .tank-preview-wall-tint-*)
// 빈 사육장은 회색, 초록·노랑은 각 벽지 이미지의 벽 색에 맞춘 필터
// TODO: 같은 원본에서 벽 색만 바꾼 빈 사육장·벽지 이미지를 받으면 이미지로 교체

// 바닥재: 사육장 바닥 마름모(무대 기준 꼭짓점 왼(80,350.7) 뒤(193.3,293.8) 오른(308.1,352.2) 앞(194.8,415))에
// 각 바닥재 이미지의 윗면을 늘려 맞추고 바닥 모양대로 잘라낸다 (이미지 위치는 tankParts의 fill)
const FLOOR_AREA = { left: 80, top: 293.8, width: 228.1, height: 121.3 }

// 위치값(px)을 감싸는 상자 기준 %로 바꾼다 → 미리보기 너비가 바뀌어도 레이어끼리 비율 그대로 커지고 줄어듦
const percent = (value, total) => `${(value / total) * 100}%`

// 무대 기준 [left, top, width] → 사육장 틀 기준 % (높이는 이미지 비율대로)
const frameStyle = ([left, top, width]) => ({
  left: percent(left - FRAME.left, FRAME.width),
  top: percent(top - FRAME.top, FRAME.height),
  width: percent(width, FRAME.width),
})

const FLOOR_STYLE = {
  ...frameStyle([FLOOR_AREA.left, FLOOR_AREA.top, FLOOR_AREA.width]),
  height: percent(FLOOR_AREA.height, FRAME.height),
}

// 바닥 영역 기준 [left, top, width, height] → 바닥 영역 기준 %
const floorFillStyle = ([left, top, width, height]) => ({
  left: percent(left, FLOOR_AREA.width),
  top: percent(top, FLOOR_AREA.height),
  width: percent(width, FLOOR_AREA.width),
  height: percent(height, FLOOR_AREA.height),
})

// 사육장 미리보기 (마이·온보딩 사육장 커스텀, 홈 일러스트 공통)
// 벽지 또는 빈 사육장 위에 바닥재·덩굴·구조물을 겹친다.
// img처럼 부모 너비를 꽉 채우고(width 100%) 사육장 이미지 비율대로 자기 높이를 가진다
// tank: { wall, floor, vine, structure } (form.tank·getMyTank()와 같은 모양, 빈 값이면 안 그림)
// className: 쓰는 화면에서 크기·위치를 조절할 때 (너비만 주면 높이는 비율대로)
const TankPreview = ({ tank, className }) => {
  const selectedOption = (part) => part.options.find((option) => option.value === tank?.[part.type])
  const [wallPart, floorPart, ...decorParts] = TANK_PARTS
  const wallTint = selectedOption(wallPart)?.value ?? 'empty'
  const floor = selectedOption(floorPart)

  return (
    <div className={className ? `tank-preview ${className}` : 'tank-preview'} aria-label="내 사육장 미리보기" role="img">
      <img className="tank-preview-wall" src={wallCream} alt="" />
      {wallTint !== 'cream' && (
        <img className={`tank-preview-wall tank-preview-wall-tint tank-preview-wall-tint-${wallTint}`} src={wallCream} alt="" />
      )}
      {floor && (
        <div className="tank-preview-floor" style={FLOOR_STYLE}>
          <div className="tank-preview-floor-fill">
            <img src={floor.image} alt="" style={floorFillStyle(floor.fill)} />
          </div>
        </div>
      )}
      {decorParts.map((part) => {
        const option = selectedOption(part)
        return (
          option && (
            <img key={part.type} className={`tank-preview-${part.type}`} src={option.image} alt="" style={frameStyle(option.rect)} />
          )
        )
      })}
    </div>
  )
}

export default TankPreview
