import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import Nav from '../../components/nav/Nav'

import background from '../../assets/guide/가이드 배경.svg'
import stepTitle from '../../assets/guide/2단계 준비.svg'
import step2intro from '../../assets/guide/1단계 인트로.svg'
import back from '../../assets/guide/뒤로가기.svg'
import enclosureButton from '../../assets/guide/준비상태확인하기.svg'
import leaf from '../../assets/guide/나뭇잎.svg'
import checkTitle from '../../assets/guide/확인해주세요.svg'
import completeButton from '../../assets/guide/완료버튼.svg'

import './Step2.scss'

const Step2 = () => {
  const navigate = useNavigate()
  const [popup, setPopup] = useState(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [checked, setChecked] = useState([false, false, false, false, false])

  const handleCheck = (index) => {
    setChecked((prev) => prev.map((item, i) => i === index ? !item : item))
  }

  const isAllChecked = checked.every((item) => item)

  return (
    <div className="step2">
      <img className="step2-background" src={background} alt="" />

      <div className="step2-scroll">
        <div className="step2-inner">

          <button className="step2-back" onClick={() => navigate('/guide')}>
            <img src={back} alt="뒤로가기" />
          </button>

          <div className="step2-progress">
            <div className="progress-dot active"></div>
            <div className="progress-line"></div>
            <div className="progress-dot active"></div>
            <div className="progress-line"></div>
            <div className="progress-dot"></div>
            <div className="progress-line"></div>
            <div className="progress-dot"></div>
            <div className="progress-line"></div>
            <div className="progress-dot"></div>
          </div>

          <img className="step2-title" src={stepTitle} alt="2단계 내부 환경 세팅" />

          <img className="step2-intro" src={step2intro} alt="" />

          <div className="step2-card card1">
            <div className="card-title">
              <span data-text="01">01</span>
              <strong>이동/은신 공간</strong>
              <button className="card-question" onClick={() => setPopup('Placement')}>어떻게 배치하면 좋을까요? 〉</button>
            </div>

            <p>
              오르고 숨을 수 있는 공간을 만들어주세요. 가지와 덩굴을 여러 높이로<br />
              연결하고, 몸을 가리고 쉴 수 있는 공간도 함께 마련해주세요.
            </p>
          </div>

          <div className="step2-card card2">
            <div className="card-title">
              <span data-text="02">02</span>
              <strong>바닥재</strong>
              <button className="card-question" onClick={() => setPopup('Substrate')}>어떤 바닥재가 좋을까요? 〉</button>
            </div>

            <p>
              성장 단계와 관리 방식에 맞는 바닥재를 준비해주세요. 습도 관리와<br />
              청소가 가능하고 개체에게 안전한 바닥재인지 확인해주세요.
            </p>
          </div>

          <div className="step2-card card3">
            <div className="card-title">
              <span data-text="03">03</span>
              <strong>물/먹이 공간</strong>
            </div>

            <p>
              편하게 접근할 수 있는 급여 공간을 마련해주세요. 깨끗한 물을<br />
              제공하고 먹이와 물그릇이 안정적으로 놓여 있는지 확인해주세요.
            </p>
          </div>

          <div className="step2-card card4">
            <div className="card-title">
              <span data-text="04">04</span>
              <strong>조명/히팅 장비</strong>
              <button className="card-question" onClick={() => setPopup('Equipment')}>어떤 장비가 필요한가요? 〉</button>
            </div>

            <p>
              빛과 온도를 관리할 장비를 준비해주세요. 낮과 밤의 주기를 위한<br />
              조명과 필요한 열원을 준비하고, 열원을 사용할 경우 온도조절기를<br />
              함께 설치해주세요.
            </p>
          </div>

          <button className="step2-button" onClick={() => setIsModalOpen(true)}>
            <img className="button-normal" src={enclosureButton} alt="준비상태확인하기" />
          </button>

        </div>
      </div>

      {popup && (
        <div className="guide-popup-overlay">
          <div className="guide-popup">

            <button className="guide-popup-close" onClick={() => setPopup(null)}>×</button>

            {popup === 'Placement' && (
              <>
                <div className="guide-popup-title">
                  <span data-text="01">01</span>
                  <strong>어떻게 배치하면 좋을까요?</strong>
                </div>

                <p className="guide-popup-text">
                  가지와 덩굴을 단순히 장식처럼 두기보다, <strong>사육장 아래에서<br />위쪽까지 이동할 수 있도록 서로 이어지게 배치해주세요.</strong>
                </p>

                <div className="guide-popup-recommend">
                  <div className="recommend-title">
                    <span className="recommend-bar"></span>
                    <strong>이렇게 배치해보세요</strong>
                  </div>

                  <p><span>높이가 다른 가지를 세로·가로·대각선 방향으로 섞어 한쪽 구조물에서 다른 구조물로 이동할 수 있게 해주세요.</span></p>
                  <p><span>높은 위치에도 머물거나 이동할 수 있는 가지와 덩굴을 마련해주세요.</span></p>
                  <p><span>몸을 가릴 공간을 함께 만들어요. 코르크 바크, 코코넛 은신처, 넓은 잎 등을 이용해 낮 동안 몸을 숨기고 쉴 수 있는 장소를 만들어주세요.</span></p>
                  <p><span>구조물이 흔들리지 않는지 확인해요. 점프했을 때 쉽게 떨어지거나 넘어지는 장식은 피해주세요.</span></p>
                </div>
              </>
            )}

            {popup === 'Substrate' && (
              <>
                <div className="guide-popup-title">
                  <span data-text="02">02</span>
                  <strong>어떤 바닥재가 좋을까요?</strong>
                </div>

                <p className="guide-popup-text">
                  <strong>바닥재는 습도 유지뿐 아니라 청소와 관찰에도 영향을 줘요.</strong>
                </p>

                <p className="guide-popup-text">
                  크레스티드 게코 사육에서는 <strong>코코넛 파이버나 토양계 바닥<br />재</strong> 등이 사용되지만, 어린 개체나 상태를 자주 관찰해야<br />
                  하는 경우에는 <strong>관리하기 쉬운 종이타월</strong>을 사용해요.
                </p>

                <div className="guide-popup-recommend">
                  <div className="recommend-title">
                    <span className="recommend-bar"></span>
                    <strong>이런 바닥재는 피해주세요</strong>
                  </div>

                  <p>나무 조각이나 우드칩처럼 삼켰을 때 문제가 될 수 있는 큰<br />입자의 바닥재는 피하는 편이 좋아요.</p>
                </div>
              </>
            )}

            {popup === 'Equipment' && (
              <>
                <div className="guide-popup-title">
                  <span data-text="04">04</span>
                  <strong>어떤 장비가 필요한가요?</strong>
                </div>

                <p className="guide-popup-text">
                  <strong>1. 주간 조명 🌞</strong><br />
                  낮과 밤을 구분할 수 있도록 일정한 주기로 빛을 제공해주세<br />요.
                  밤에는 가시광 조명을 끄는 것이 좋아요. 타이머를 사용<br />하면
                  매일 매일 비슷한 시간에 켜고 끌 수 있어요.
                </p>

                <p className="guide-popup-text">
                  <strong>2. 히팅 장비 🔥 <span style={{ color: '#FF9834', fontSize: '13px', fontWeight: 500 }}>필요한 경우</span></strong><br />
                  실내 온도만으로 필요한 온도 환경을 유지하기 어렵다면 열<br />원을
                  추가할 수 있어요. 열램프나 세라믹 히터 등의 선택지<br />는 있지만
                  사육장의 실제 측정 온도를 먼저 확인한 뒤 결정<br />해주세요.
                </p>

                <p className="guide-popup-text">
                  <strong>3. 온도조절기 🌡️ <span style={{ color: '#FF9834', fontSize: '13px', fontWeight: 500 }}>열원 사용 시 필수</span></strong><br />
                  열원을 사용한다면 온도조절기(thermostat)를 함께 연결<br />해주세요.
                  설정 온도를 넘어 계속 가열되는 상황을 방지하는<br />역할을 해요.
                </p>

                <div className="guide-popup-recommend">
                  <div className="recommend-title">
                    <span className="recommend-bar"></span>
                    <strong>설치 전 꼭 확인해주세요</strong>
                  </div>

                  <p>
                    열원은 도마뱀이 직접 닿아 화상을 입지 않도록 설치해야
                    해요. 열램프는 사육장 외부 또는 적절한 가드와 함께 사용하고,
                    핫락(가열 돌)은 사용하지 않는 것이 좋아요.
                  </p>
                </div>
              </>
            )}

          </div>
        </div>
      )}

      {isModalOpen && (
        <div className="preparation-overlay" onClick={() => setIsModalOpen(false)}>
          <div className="preparation-modal" onClick={(e) => e.stopPropagation()}>

            <div className="preparation-title">
              <img className="preparation-leaf" src={leaf} alt="" />
              <img className="preparation-title-text" src={checkTitle} alt="준비항목을 확인해주세요." />
            </div>

            <div className="preparation-list">

              <label className="preparation-item">
                <input type="checkbox" checked={checked[0]} onChange={() => handleCheck(0)} />
                <div>
                  <strong>가지와 덩굴을 여러 높이에 연결해 설치했어요</strong>
                  <p>위아래로 이동할 수 있는 경로가 있어요.</p>
                </div>
              </label>

              <label className="preparation-item">
                <input type="checkbox" checked={checked[1]} onChange={() => handleCheck(1)} />
                <div>
                  <strong>몸을 충분히 가릴 수 있는 은신 공간을 마련했어요</strong>
                  <p>잎이나 은신처처럼 편하게 숨어 쉴 공간이 있어요.</p>
                </div>
              </label>

              <label className="preparation-item">
                <input type="checkbox" checked={checked[2]} onChange={() => handleCheck(2)} />
                <div>
                  <strong>성장 단계와 관리 방식에 맞는 바닥재를 준비했어요</strong>
                  <p>바닥재가 지나치게 젖어 있거나 오염되어 있지 않아요.</p>
                </div>
              </label>

              <label className="preparation-item">
                <input type="checkbox" checked={checked[3]} onChange={() => handleCheck(3)} />
                <div>
                  <strong>물과 먹이에 편하게 접근할 수 있는 공간을 마련했어요</strong>
                  <p>그릇이 쉽게 넘어지지 않도록 안정적으로 배치했어요.</p>
                </div>
              </label>

              <label className="preparation-item">
                <input type="checkbox" checked={checked[4]} onChange={() => handleCheck(4)} />
                <div>
                  <strong>낮과 밤을 구분할 수 있는 조명을 준비했어요</strong>
                  <p>일정한 주기로 조명을 켜고 끌 수 있어요.</p>
                </div>
              </label>

            </div>

            <button
              className="preparation-complete"
              disabled={!isAllChecked}
              onClick={() => navigate('/guide/step3')}
            >
              <img src={completeButton} alt="완료" />
            </button>

          </div>
        </div>
      )}

    </div>
  )
}

export default Step2