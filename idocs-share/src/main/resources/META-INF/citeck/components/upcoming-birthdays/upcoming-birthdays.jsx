import React from "ecosui!react";
import "xstyle!./upcoming-birthdays.css";

export class UpcomingBirthdays extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            data: []
        };
    }

    componentDidMount() {
        let url = Alfresco.constants.PROXY_URI + "citeck/people/upcoming-birthdays";
        fetch(url, {
            credentials: 'include'
        }).then(response => {
            return response.json();
        }).then(json => {
            this.setState({
                data: json
            });
        });
    }

    render() {
        let defaultAvatar = Alfresco.constants.URL_RESCONTEXT + "citeck/components/upcoming-birthdays/images/avatar.png";
        let avatarPrefix = Alfresco.constants.PROXY_URI + "api/node/content;ecos:photo/workspace/SpacesStore/";
    return (
        <div>
            {this.state.data.map((person, index) => (
                <div className="birthday-record" key={person.id}>
                    <div className="avatar">
                        <img className="user-photo" src={String(person.hasphoto) === "true" ? avatarPrefix + person.id + "/" + person.username : defaultAvatar} alt={person.firstname}/>
                    </div>
                    <div className="content">
                        <span><a href={Alfresco.constants.URL_PAGECONTEXT + "user/" + person.username + "/profile"}>{person.firstname} {person.lastname}</a></span>
                        <br />
                        <span>{UpcomingBirthdays.getLocalizedDate(person.birthdate)}</span>
                    </div>
                </div>
            ))}
        </div>
    );
    }

    static getLocalizedDate(dateString) {
        let date = new Date(dateString);
        let lang = Alfresco.constants.JS_LOCALE.substr(0, 2);
        return date.toLocaleString(lang, { month: 'long', day: 'numeric' });
    }
}