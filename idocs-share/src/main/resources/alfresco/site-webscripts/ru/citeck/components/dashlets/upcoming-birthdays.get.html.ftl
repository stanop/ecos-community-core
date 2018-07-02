<@markup id="widgets">
    <@createWidgets group="dashlets"/>
</@>

<@markup id="html">
    <@uniqueIdDiv>
        <#assign id = args.htmlid?html>
    <div class="dashlet">
        <div class="title">${msg("label.header")}</div>

        <div class="body scrollableList" <#if args.height??>style="height: ${args.height?html}px;"</#if>>
            <div id="${id}-birtdays_element"></div>
        </div>
    </div>

    <script type="text/javascript">
        require(['citeck/components/upcoming-birthdays/upcoming-birthdays',
                    'react-dom', 'react'],
                function(component, ReactDOM, React) {
                    ReactDOM.render(
                            React.createElement(component.UpcomingBirthdays),
                            document.getElementById('${id}-birtdays_element')
                    );
                });
    </script>
    </@>
</@>