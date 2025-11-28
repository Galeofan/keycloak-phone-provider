<#import "footer.ftl" as loginFooter>
<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
    <!DOCTYPE html>
    <html class="${properties.kcHtmlClass!}" lang="${lang}"<#if realm.internationalizationEnabled> dir="${(locale.rtl)?then('rtl','ltr')}"</#if>>

        <head>
            <meta charset="utf-8">
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
            <meta name="robots" content="noindex, nofollow">

            <style>
                .${properties.kcLoginClass!} {
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
                }

                .${properties.kcFormCardClass!} {
                margin: 0;
                transform: translateY(-5vh); /* поднимаем форму наверх */
                }

                .kc-header-with-logo {
                display: flex;
                align-items: flex-start;
                gap: 6px; /* расстояние между логотипом и заголовком */
                }

                .kc-header-logo {
                height: 64px; /* уменьшает логотип */
                width: auto;
                position: relative !important;
                top: 12px !important;
                display: inline-block; /* важно для корректного смещения */
                }
            </style>

            <#if properties.meta?has_content>
                <#list properties.meta?split(' ') as meta>
                    <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
                </#list>
            </#if>
            <title>${msg("loginTitle",(realm.displayName!''))}</title>
            <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" />
            <#if properties.stylesCommon?has_content>
                <#list properties.stylesCommon?split(' ') as style>
                    <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet" />
                </#list>
            </#if>
            <#if properties.styles?has_content>
                <#list properties.styles?split(' ') as style>
                    <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
                </#list>
            </#if>
            <#if properties.scripts?has_content>
                <#list properties.scripts?split(' ') as script>
                    <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
                </#list>
            </#if>
            <script type="importmap">
                {
                "imports": {
                "rfc4648": "${url.resourcesCommonPath}/vendor/rfc4648/rfc4648.js"
                }
                }
            </script>
            <script src="${url.resourcesPath}/js/menu-button-links.js" type="module"></script>
            <#if scripts??>
                <#list scripts as script>
                    <script src="${script}" type="text/javascript"></script>
                </#list>
            </#if>
            <script type="module">
                import { startSessionPolling } from "${url.resourcesPath}/js/authChecker.js";

                startSessionPolling(
                "${url.ssoLoginInOtherTabsUrl?no_esc}"
                );
            </script>
            <#if authenticationSession??>
                <script type="module">
                    import { checkAuthSession } from "${url.resourcesPath}/js/authChecker.js";

                    checkAuthSession(
                    "${authenticationSession.authSessionIdHash}"
                    );
                </script>
            </#if>
        </head>

        <body class="${properties.kcBodyClass!}" data-page-id="login-${pageId}">
            <div class="${properties.kcLoginClass!}">
                <#-- Убрали блок отображения имени Realm -->
                <div class="${properties.kcFormCardClass!}">
                    <header class="${properties.kcFormHeaderClass!}">
                        <div class="kc-header-with-logo">
                            <img src="${url.resourcesPath}/img/mt-logo.svg"
                            alt="logo"
                            class="kc-header-logo"/>

                            <h1 id="kc-page-title">
                                <#nested "header">
                            </h1>
                        </div>
                    </header>

                    <div id="kc-content">
                        <div id="kc-content-wrapper">

                            <#-- App-initiated actions should not see warning messages about the need to complete the action -->
                            <#-- during login.                                                                               -->
                            <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
                                <div class="alert-${message.type} ${properties.kcAlertClass!} pf-m-<#if message.type = 'error'>danger<#else>${message.type}</#if>">
                                    <div class="pf-c-alert__icon">
                                        <#if message.type = 'success'><span class="${properties.kcFeedbackSuccessIcon!}"></span></#if>
                                        <#if message.type = 'warning'><span class="${properties.kcFeedbackWarningIcon!}"></span></#if>
                                        <#if message.type = 'error'><span class="${properties.kcFeedbackErrorIcon!}"></span></#if>
                                        <#if message.type = 'info'><span class="${properties.kcFeedbackInfoIcon!}"></span></#if>
                                    </div>
                                    <span class="${properties.kcAlertTitleClass!}">${kcSanitize(message.summary)?no_esc}</span>
                                </div>
                            </#if>

                            <#nested "form">

                            <#if auth?has_content && auth.showTryAnotherWayLink()>
                                <form id="kc-select-try-another-way-form" action="${url.loginAction}" method="post">
                                    <div class="${properties.kcFormGroupClass!}">
                                        <input type="hidden" name="tryAnotherWay" value="on"/>
                                        <a href="#" id="try-another-way"
                                        onclick="document.forms['kc-select-try-another-way-form'].requestSubmit();return false;">${msg("doTryAnotherWay")}</a>
                                    </div>
                                </form>
                            </#if>

                            <#nested "socialProviders">

                            <#if displayInfo>
                                <div id="kc-info" class="${properties.kcSignUpClass!}">
                                    <div id="kc-info-wrapper" class="${properties.kcInfoAreaWrapperClass!}">
                                        <#nested "info">
                                    </div>
                                </div>
                            </#if>
                        </div>
                    </div>

                    <@loginFooter.content/>
                </div>
            </div>
        </body>
    </html>
</#macro>
