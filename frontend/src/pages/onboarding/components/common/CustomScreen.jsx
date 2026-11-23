import OnboardingHeader from './OnboardingHeader'
import OnboardingProgress from './OnboardingProgress'
import SoundToggle from './SoundToggle'

import './CustomScreen.scss'

// 도마뱀·사육장 커스텀 공통 화면 틀 (피그마 393 폭)
// 위쪽 무대(배경·진행바·배지·스피커·미리보기·탭)는 고정, 아래 패널 안에서만 스크롤한다.
// stage: 미리보기 요소 (화면별 SCSS에서 절대 위치 지정)
// badge: { image, label, onClick }, tabs: [{ key, label, icon, iconSize: [w, h] }]
// progress·badge·onToggleSound는 선택 (마이 > 커스텀 화면에는 없음)
const CustomScreen = ({
  className,
  title,
  onBack,
  background,
  progress,
  badge,
  notice,
  isSoundOn,
  onToggleSound,
  soundSize,
  stage,
  tabs,
  activeTab,
  onTabChange,
  children,
}) => {
  return (
    <section className={`custom-screen ${className}`}>
      <div className="custom-screen-stage" style={{ backgroundImage: `url(${background})` }}>
        <OnboardingHeader title={title} onBack={onBack} />

        {(progress !== undefined || badge) && (
          <div className="custom-screen-progress">
            {progress !== undefined && <OnboardingProgress value={progress} />}
            {badge && (
              <button type="button" className="custom-screen-badge" aria-label={badge.label} onClick={badge.onClick}>
                <img src={badge.image} alt="" />
              </button>
            )}
          </div>
        )}
        {notice && <p className="custom-screen-notice" role="alert">{notice}</p>}

        {onToggleSound && (
          <div className="custom-screen-sound">
            <SoundToggle isOn={isSoundOn} onToggle={onToggleSound} size={soundSize} />
          </div>
        )}

        {stage}

        <div className="custom-screen-tabs" role="tablist">
          {tabs.map((tab) => (
            <button
              key={tab.key}
              type="button"
              role="tab"
              aria-selected={activeTab === tab.key}
              aria-label={tab.label}
              className={activeTab === tab.key ? 'custom-screen-tab active' : 'custom-screen-tab'}
              onClick={() => onTabChange(tab.key)}
            >
              <img src={tab.icon} alt="" style={{ width: tab.iconSize[0], height: tab.iconSize[1] }} />
            </button>
          ))}
        </div>
      </div>

      <div className="custom-screen-panel" role="tabpanel">
        {children}
      </div>
    </section>
  )
}

export default CustomScreen
