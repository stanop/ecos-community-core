import React, {Fragment} from 'react';
import ReactDOM from "react-dom";
import "xstyle!./slide-menu.css";

class SlideMenu extends React.Component {
    constructor(props) {
        super(props);

        this.checkbox = document.createElement('input');
        this.checkbox.id = 'slide-menu-checkbox';
        this.checkbox.type = 'checkbox';

        this.menu = document.createElement('div');
        this.menu.classList.add('slide-menu');

        this.mask = document.createElement('div');
        this.mask.classList.add('mask-content');

        const maskLabel = document.createElement('label');
        maskLabel.setAttribute("for", "slide-menu-checkbox");
        this.mask.appendChild(maskLabel);
    }

    componentDidMount() {
        const theFirstChild = document.body.firstChild;
        document.body.insertBefore(this.checkbox, theFirstChild);
        document.body.insertBefore(this.menu, theFirstChild);
        document.body.insertBefore(this.mask, theFirstChild);
    }

    componentWillUnmount() {
        document.body.removeChild(this.checkbox);
        document.body.removeChild(this.menu);
        document.body.removeChild(this.mask);
    }

    render() {
        return ReactDOM.createPortal(
            <Fragment>
                <label className='slide-menu-toggle' htmlFor="slide-menu-checkbox" />
                <nav>TODO</nav>
            </Fragment>,
            this.menu,
        );
    }
}

export default SlideMenu;