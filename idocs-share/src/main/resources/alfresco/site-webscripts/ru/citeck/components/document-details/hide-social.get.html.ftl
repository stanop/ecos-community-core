<#assign cfg = config.scoped['ModuleConfig.node-header']!{} />
<style>
/* ${config.scoped['ModuleConfig.node-header']['hide-node-path']} */
<#if cfg['hide-node-path'].value == 'true'>.node-info .node-path,</#if>
<#if cfg['hide-modifier'].value == 'true'>.node-info .item-modifier,</#if>
<#if cfg['hide-social'].value == 'true'>.node-info .item-social,</#if>
<#if cfg['hide-favourite'].value == 'true'>.node-info .item-social .favourite-action,</#if>
<#if cfg['hide-likes'].value == 'true'>.node-info .item-social .like-action,</#if>
<#if cfg['hide-likes'].value == 'true'>.node-info .item-social .likes-count,</#if>
<#if cfg['hide-share'].value == 'true'>.node-info .item-social .quickshare-action,</#if>
<#if cfg['hide-share'].value == 'true'>.node-info .item-social .quickshare-indicator,</#if>
non-existing-hidden-class
{
    display: none;
}
</style>