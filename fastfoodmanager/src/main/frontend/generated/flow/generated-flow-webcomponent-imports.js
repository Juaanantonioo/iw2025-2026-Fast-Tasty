import { injectGlobalWebcomponentCss } from 'Frontend/generated/jar-resources/theme-util.js';

import { injectGlobalCss } from 'Frontend/generated/jar-resources/theme-util.js';

import { css, unsafeCSS, registerStyles } from '@vaadin/vaadin-themable-mixin';
import $cssFromFile_0 from 'Frontend/themes/my-theme/login.css?inline';
import $cssFromFile_1 from 'Frontend/themes/my-theme/welcome.css?inline';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/text-field/theme/lumo/vaadin-text-field.js';
import '@vaadin/dialog/theme/lumo/vaadin-dialog.js';
import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/login/theme/lumo/vaadin-login-form.js';
import '@vaadin/vertical-layout/theme/lumo/vaadin-vertical-layout.js';
import '@vaadin/tooltip/theme/lumo/vaadin-tooltip.js';
import '@vaadin/button/theme/lumo/vaadin-button.js';
import 'Frontend/generated/jar-resources/disableOnClickFunctions.js';
import '@vaadin/integer-field/theme/lumo/vaadin-integer-field.js';
import '@vaadin/date-picker/theme/lumo/vaadin-date-picker.js';
import 'Frontend/generated/jar-resources/datepickerConnector.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-layout.js';
import '@vaadin/text-area/theme/lumo/vaadin-text-area.js';
import '@vaadin/email-field/theme/lumo/vaadin-email-field.js';
import '@vaadin/tabs/theme/lumo/vaadin-tab.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-item.js';
import '@vaadin/tabs/theme/lumo/vaadin-tabs.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-row.js';
import '@vaadin/time-picker/theme/lumo/vaadin-time-picker.js';
import 'Frontend/generated/jar-resources/vaadin-time-picker/timepickerConnector.js';
import '@vaadin/notification/theme/lumo/vaadin-notification.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';
import 'Frontend/generated/jar-resources/ReactRouterOutletElement.tsx';

injectGlobalCss($cssFromFile_0.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_0.toString());

injectGlobalCss($cssFromFile_1.toString(), 'CSSImport end', document);
injectGlobalWebcomponentCss($cssFromFile_1.toString());

const loadOnDemand = (key) => {
  const pending = [];
  if (key === '4dcb4b57035163df8d4f12639bf4d2a19933cbc4461e8cb0b9e142dda11c2b4a') {
    pending.push(import('./chunks/chunk-7f51ee37207ff32525a9f10e22f0e525d0c504d7dd783bf192c5839d89491441.js'));
  }
  if (key === '43a5f7bf517794d04df3ed5fc98dc95245aaa7f48f12cd09005c8519f29f99c5') {
    pending.push(import('./chunks/chunk-94a0d51bd0a9d31ea6d0a35b626cba30a0fd2e28bf1f4c90def369f3e1bc2d4d.js'));
  }
  if (key === '4df37f6098db1f46c3a2a10a8a6ee884d7703cc4341d5f6ca9924d06e3bd8d5f') {
    pending.push(import('./chunks/chunk-12befbafcb5f2a04df71ea7ffb287f5eba68e7d54e5d25ffe135761c2976577e.js'));
  }
  if (key === 'b19ebaa52e640cf739905db2a5fe19399e26fbc885fafadbd2102b2e1114ad58') {
    pending.push(import('./chunks/chunk-71f8a81aa29eeaff3407092ac5ceb49ad73fee9af3d8d05dfc5cef815cfe696b.js'));
  }
  if (key === '7a16c5126a9f665b5c7fb3ed64d9f43921c30bb8a2d4c8d74391dcbed6010738') {
    pending.push(import('./chunks/chunk-71f8a81aa29eeaff3407092ac5ceb49ad73fee9af3d8d05dfc5cef815cfe696b.js'));
  }
  if (key === '888f56b59b8b66e34ed7fcabd38363d2727b102216a9a9b399e86ab0f69c68bd') {
    pending.push(import('./chunks/chunk-7f51ee37207ff32525a9f10e22f0e525d0c504d7dd783bf192c5839d89491441.js'));
  }
  if (key === '64a61fb9812cca685f5cf93ad60c505c34ac8f0b478a141845b0452b159a5a00') {
    pending.push(import('./chunks/chunk-18e59031384a6c026613a8473e46d0f98e2aa47f7e23a7525fe5740505717e35.js'));
  }
  if (key === '624d8824303296038d2df0c14156f1b5582a45d99f0876bf80877c9f5d3db5c5') {
    pending.push(import('./chunks/chunk-5bbdf8feff85ca22816aafadcbe00257e3377f6341dde0ee343f567af818e2cf.js'));
  }
  if (key === '094e44f94cea6dcd44b4032c475eb933b1650308435070370b41a7f4cc6d8cfd') {
    pending.push(import('./chunks/chunk-ceb2c9c8b19d2f1c8a155867d6d50be972f8bf893685ff529ab595e0725c6802.js'));
  }
  if (key === 'bbd4949900965d8d3e58e0d0083db0b6e08393fc5c7955960f49ab6c8b05714d') {
    pending.push(import('./chunks/chunk-7f51ee37207ff32525a9f10e22f0e525d0c504d7dd783bf192c5839d89491441.js'));
  }
  if (key === '4b8344f950ff7d45aa299af9f26e25355bcfdcf34230d0f95d915af969319bf1') {
    pending.push(import('./chunks/chunk-52ff183c3b161a6396f31bed75cbd47ed003016118e641e37a7314d44a96ad6c.js'));
  }
  if (key === '3c00f8e5d2c4f89d8e206d899efe51d79cb7d39b511ce2405552655b015057e3') {
    pending.push(import('./chunks/chunk-3be914412e5a9aa64e2a5748b11047fa3e7cff02dead0408a8e5a0077965d59a.js'));
  }
  if (key === '82f8d1205a47d41c7d4bcdacfea8ae7e73e3365d3e4066dbc2abf96b7ac1f681') {
    pending.push(import('./chunks/chunk-3caf9345c22c47f8bcdc7012b0c6b5eb798234d94097f8b3ec61713e34a41f96.js'));
  }
  if (key === '3d46d30519c827ab35c8f9e16f55e6e90945f36cdaa5f4844608ca304536d4b5') {
    pending.push(import('./chunks/chunk-18e59031384a6c026613a8473e46d0f98e2aa47f7e23a7525fe5740505717e35.js'));
  }
  if (key === '3ae3b5e86d2af934336332981d7fa9278284ef62cab95cc7abd785e43b2ffdd8') {
    pending.push(import('./chunks/chunk-94a0d51bd0a9d31ea6d0a35b626cba30a0fd2e28bf1f4c90def369f3e1bc2d4d.js'));
  }
  if (key === 'dda9438d6545b9968b6e81dc970de5bdc1962c2d206edc88fde34cd31c5e4556') {
    pending.push(import('./chunks/chunk-ce51ca8957a7d29a6e2246747d399a672c8722f9f9ccf0cb83ee87fe5566edd5.js'));
  }
  if (key === '50f64423eeb5aa97541473260fcca1e0776e1e1f6cd2b832cb47c828b5d9f48a') {
    pending.push(import('./chunks/chunk-52ff183c3b161a6396f31bed75cbd47ed003016118e641e37a7314d44a96ad6c.js'));
  }
  if (key === '2203ec0c94e2866f9fd07380ae053e92ed0199790575d875d210f31faf713d13') {
    pending.push(import('./chunks/chunk-7f51ee37207ff32525a9f10e22f0e525d0c504d7dd783bf192c5839d89491441.js'));
  }
  if (key === '411b46b3d2650e79a00d04bc23611c30a81992cfaa958c948a9b2a0027f99b65') {
    pending.push(import('./chunks/chunk-71f8a81aa29eeaff3407092ac5ceb49ad73fee9af3d8d05dfc5cef815cfe696b.js'));
  }
  if (key === '029d89db277b2766d01e05b15cca9571e1c80222fd649cce9c05f616b1114a53') {
    pending.push(import('./chunks/chunk-7f51ee37207ff32525a9f10e22f0e525d0c504d7dd783bf192c5839d89491441.js'));
  }
  if (key === '6f9d07d801f469a89518c79caec1b2f3d5e9bbe419af8ec821975d66a1902bae') {
    pending.push(import('./chunks/chunk-7f51ee37207ff32525a9f10e22f0e525d0c504d7dd783bf192c5839d89491441.js'));
  }
  if (key === 'c2fa3eeb5c4bc010a07911c2af6a4379e73875742396dbcdfcb649df71c5c1de') {
    pending.push(import('./chunks/chunk-3caf9345c22c47f8bcdc7012b0c6b5eb798234d94097f8b3ec61713e34a41f96.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}