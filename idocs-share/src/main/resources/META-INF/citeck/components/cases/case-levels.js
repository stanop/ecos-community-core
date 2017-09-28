define(['citeck/utils/knockout.utils'], function(koutils) {
    
var koclass = koutils.koclass;

var s = String,
    b = Boolean,
    Case = koclass('cases.completeness.Case'),
    Level = koclass('cases.completeness.Level'),
    Requirement = koclass('cases.completeness.Requirement'),
    Match = koclass('cases.completeness.Match');

Case
    .key('nodeRef', s)
    .property('levels', [Level])
    .method('hasLevels', function() {
        var levels = this.levels();
        return levels != null && levels.length > 0;
    })
    .load('levels', koutils.simpleLoad({
        url: Alfresco.constants.PROXY_URI + "citeck/cases/levels?nodeRef={nodeRef}"
    }))
    ;

Level
    .key('nodeRef', s)
    .property('title', s)
    .property('description', s)
    .property('current', b)
    .property('completed', b)
    .property('requirements', [Requirement])
    .property('_opened', b)
    .computed('opened', {
        read: function() {
            var opened = this._opened();
            return opened != null ? opened : !this.completed() && this.current();
        },
        write: function(value) {
            this._opened(value);
        }
    })
    .computed('closed', function() { return !this.opened(); })
    .method('toggle', function() {
        this.opened(!this.opened());
    })
    ;

Requirement
    .key('nodeRef', s)
    .property('title', s)
    .property('description', s)
    .property('passed', b)
    .property('matches', [Match])
    .method('hasMatches', function() {
        var matches = this.matches();
        return matches != null && matches.length > 0;
    })
    .property('opened', b)
    .computed('closed', function() { return !this.opened(); })
    .method('toggle', function() {
        this.opened(!this.opened());
    })
    ;

Match
    .key('nodeRef', s)
    .property('name', s)
    ;
    
})