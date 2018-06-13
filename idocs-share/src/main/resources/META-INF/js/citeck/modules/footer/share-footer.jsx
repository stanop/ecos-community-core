
import React from 'react';
import SurfRegion from "../surf/surf-region";

const ShareFooter = function ({theme, cacheBust}) {

    return <div id="alf-ft">
        <SurfRegion className="sticky-footer" args={{
            regionId: "footer",
            scope: "global",
            pageid: "card-details",
            theme: theme,
            cacheAge: 600,
            cb: cacheBust
        }}/>
    </div>;
};

export default ShareFooter;
