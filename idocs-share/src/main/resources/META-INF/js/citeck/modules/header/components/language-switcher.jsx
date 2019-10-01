import React from 'ecosui!react';
import { COOKIE_KEY_LOCALE, COOKIE_KEY_LOCALE_MAX_AGE, getCurrentLocale, setCookie } from '../../common/util';

const LANGUAGE_RU = "ru";
const LANGUAGE_EN = "en";

export default class LanguageSwitcher extends React.Component {
    state = {
        isShow: false,
        language: LANGUAGE_EN
    };

    componentDidMount() {
        window.Citeck.Records.get('uiserv/config@language-switcher-enabled').load('value?bool').then(isEnabled => {
            if (isEnabled) {
                this.setState({ isShow: true })
            }
        });

        const locale = getCurrentLocale();
        this.setState({ language: locale })
    }

    render() {
        const { isShow, language } = this.state;
        if (!isShow) {
            return null;
        }

        return (
            <div onClick={this.switchLanguage} className='alfresco-header__language-switcher'>
                <img src={`/share/res/components/images/language-flags/${language}.png`} alt={language} />
            </div>
        );
    }

    switchLanguage = () => {
        const { language: currentLanguage } = this.state;

        const language = currentLanguage === LANGUAGE_EN ? LANGUAGE_RU : LANGUAGE_EN;

        setCookie(COOKIE_KEY_LOCALE, language, {'max-age': COOKIE_KEY_LOCALE_MAX_AGE});

        window.location.reload();
    };
}