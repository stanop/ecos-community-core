import React from 'react';
import { Scrollbars } from 'react-custom-scrollbars';

const logoLinkHref = '/share/page';

const LogoBlock = ({smallLogo, largeLogo}) => (
    <div className='slide-menu-logo'>
        <img className='slide-menu-logo__small' src={smallLogo} />
        <div className='slide-menu-logo__large'>
            <a href={logoLinkHref}>
                <img src={largeLogo} />
            </a>
        </div>
    </div>
);

export default LogoBlock;
