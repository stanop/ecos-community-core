import React from 'react';
import { Scrollbars } from 'react-custom-scrollbars';

export const LogoBlock = ({smallLogo, largeLogo}) => {
    const logoLinkHref = '/share/page';

    return (
        <div className='slide-menu-logo'>
            <img className='slide-menu-logo__small' src={smallLogo} />
            <div className='slide-menu-logo__large'>
                <a href={logoLinkHref}>
                    <img src={largeLogo} />
                </a>
            </div>
        </div>
    );
};

export const ListBlock = () => {
    const scrollBarStyle = { height: 'calc(100% - 40px)' };
    const verticalTrack = props => <div {...props} className="slide-menu-list__vertical-track"/>;
    return (
        <Scrollbars
            className='slide-menu-list'
            autoHide
            style={scrollBarStyle}
            renderTrackVertical={verticalTrack}
        >
            <nav>

            </nav>
        </Scrollbars>
    );
};