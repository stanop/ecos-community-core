/**
 * Admin Console Application Tool component
 */

function main()
{
    model.themes = [];

    // retrieve the available theme objects
    var themes = sitedata.getObjects("theme");
    for (var i=0, t; i<themes.length; i++) {
        t = themes[i];
        model.themes.push({
                id: t.id,
                title: (t.titleId != null && msg.get(t.titleId) != t.titleId ? msg.get(t.titleId) : t.title),
                // current theme ID is in the default model for a script
                selected: (t.id == theme)
            });
    }

    // main logo image override
    model.logo = context.getSiteConfiguration().getProperty("logo");

    // Widget instantiation metadata...
    var defaultlogo = msg.get("header.logo") == "header.logo" ? "app-logo.png" : msg.get("header.logo");

    // mobile logo image override
    model.mobileLogo = context.getSiteConfiguration().getProperty("mobileLogo");

    // Widget instantiation metadata...
    var defaultMobilelogo = msg.get("header.mobile-logo") == "header.mobile-logo" ? "app-logo-mobile.png" : msg.get("header.mobile-logo");

    var widget = {
        id: "ConsoleApplication",
        name: "Citeck.ConsoleApplication",
        options: {
            defaultlogo: url.context + "/res/themes/" + theme + "/images/" + defaultlogo,
            defaultMobilelogo: url.context + "/res/themes/" + theme + "/images/" + defaultMobilelogo
        }
    };
    model.widgets = [widget];
    var editionInfo = context.properties["editionInfo"].edition;
    model.isEnterprise = "ENTERPRISE" == editionInfo;
}

main();