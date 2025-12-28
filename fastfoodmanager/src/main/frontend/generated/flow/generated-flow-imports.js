import { injectGlobalCss } from 'Frontend/generated/jar-resources/theme-util.js';

import { css, unsafeCSS, registerStyles } from '@vaadin/vaadin-themable-mixin';
import $cssFromFile_0 from 'Frontend/themes/my-theme/login.css?inline';
import $cssFromFile_1 from 'Frontend/themes/my-theme/welcome.css?inline';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/login/theme/lumo/vaadin-login-form.js';
import '@vaadin/vertical-layout/theme/lumo/vaadin-vertical-layout.js';
import '@vaadin/text-field/theme/lumo/vaadin-text-field.js';
import '@vaadin/integer-field/theme/lumo/vaadin-integer-field.js';
import '@vaadin/date-picker/theme/lumo/vaadin-date-picker.js';
import 'Frontend/generated/jar-resources/datepickerConnector.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-layout.js';
import '@vaadin/text-area/theme/lumo/vaadin-text-area.js';
import '@vaadin/email-field/theme/lumo/vaadin-email-field.js';
import '@vaadin/tooltip/theme/lumo/vaadin-tooltip.js';
import '@vaadin/tabs/theme/lumo/vaadin-tab.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-item.js';
import '@vaadin/tabs/theme/lumo/vaadin-tabs.js';
import '@vaadin/button/theme/lumo/vaadin-button.js';
import 'Frontend/generated/jar-resources/disableOnClickFunctions.js';
import '@vaadin/form-layout/theme/lumo/vaadin-form-row.js';
import '@vaadin/time-picker/theme/lumo/vaadin-time-picker.js';
import 'Frontend/generated/jar-resources/vaadin-time-picker/timepickerConnector.js';
import '@vaadin/notification/theme/lumo/vaadin-notification.js';
import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/color-global.js';
import '@vaadin/vaadin-lumo-styles/typography-global.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';
import 'Frontend/generated/jar-resources/ReactRouterOutletElement.tsx';

injectGlobalCss($cssFromFile_0.toString(), 'CSSImport end', document);

injectGlobalCss($cssFromFile_1.toString(), 'CSSImport end', document);

const loadOnDemand = (key) => {
  const pending = [];
  if (key === '6f9d07d801f469a89518c79caec1b2f3d5e9bbe419af8ec821975d66a1902bae') {
    pending.push(import('./chunks/chunk-5d66b53c0af592fd2b5ee3d2930df4046cf7a483dad70ab4dbf7fd7a719b0174.js'));
  }
  if (key === '82f8d1205a47d41c7d4bcdacfea8ae7e73e3365d3e4066dbc2abf96b7ac1f681') {
    pending.push(import('./chunks/chunk-fb5c5bc95a70406f1945e41f9f46dd108152083eb2127d3619838032bee06385.js'));
  }
  if (key === '624d8824303296038d2df0c14156f1b5582a45d99f0876bf80877c9f5d3db5c5') {
    pending.push(import('./chunks/chunk-cc98e76d9fba8f0c86d34f5781af43b87b6d5b5c1ced77fc041802272a806483.js'));
  }
  if (key === '094e44f94cea6dcd44b4032c475eb933b1650308435070370b41a7f4cc6d8cfd') {
    pending.push(import('./chunks/chunk-30057e051c5d5072ed9cf776c10966deed935daf59dc36ddc04e9f1a5b0d469a.js'));
  }
  if (key === 'bbd4949900965d8d3e58e0d0083db0b6e08393fc5c7955960f49ab6c8b05714d') {
    pending.push(import('./chunks/chunk-2e0431a30f992e2e0a6446df08cb868beee6da091d6d7994751586ad45b8a324.js'));
  }
  if (key === '3ae3b5e86d2af934336332981d7fa9278284ef62cab95cc7abd785e43b2ffdd8') {
    pending.push(import('./chunks/chunk-6e2de0ee79f80103ec4c879ab8b286b845d6a797cf50870047de04156768057a.js'));
  }
  if (key === '64a61fb9812cca685f5cf93ad60c505c34ac8f0b478a141845b0452b159a5a00') {
    pending.push(import('./chunks/chunk-cc00e46ff9c34e7a8ce75740e5cf4f3ea4b3fe6a572da809764326faac862edb.js'));
  }
  if (key === '43a5f7bf517794d04df3ed5fc98dc95245aaa7f48f12cd09005c8519f29f99c5') {
    pending.push(import('./chunks/chunk-6e2de0ee79f80103ec4c879ab8b286b845d6a797cf50870047de04156768057a.js'));
  }
  if (key === '888f56b59b8b66e34ed7fcabd38363d2727b102216a9a9b399e86ab0f69c68bd') {
    pending.push(import('./chunks/chunk-2e0431a30f992e2e0a6446df08cb868beee6da091d6d7994751586ad45b8a324.js'));
  }
  if (key === '4df37f6098db1f46c3a2a10a8a6ee884d7703cc4341d5f6ca9924d06e3bd8d5f') {
    pending.push(import('./chunks/chunk-d5498b855750fa7a8c06f19b84f4d83be6593eb71873002b5a17c1265f16455a.js'));
  }
  if (key === '7a16c5126a9f665b5c7fb3ed64d9f43921c30bb8a2d4c8d74391dcbed6010738') {
    pending.push(import('./chunks/chunk-cc98e76d9fba8f0c86d34f5781af43b87b6d5b5c1ced77fc041802272a806483.js'));
  }
  if (key === 'b19ebaa52e640cf739905db2a5fe19399e26fbc885fafadbd2102b2e1114ad58') {
    pending.push(import('./chunks/chunk-cc98e76d9fba8f0c86d34f5781af43b87b6d5b5c1ced77fc041802272a806483.js'));
  }
  if (key === '50f64423eeb5aa97541473260fcca1e0776e1e1f6cd2b832cb47c828b5d9f48a') {
    pending.push(import('./chunks/chunk-cc98e76d9fba8f0c86d34f5781af43b87b6d5b5c1ced77fc041802272a806483.js'));
  }
  if (key === '029d89db277b2766d01e05b15cca9571e1c80222fd649cce9c05f616b1114a53') {
    pending.push(import('./chunks/chunk-2e0431a30f992e2e0a6446df08cb868beee6da091d6d7994751586ad45b8a324.js'));
  }
  if (key === 'dda9438d6545b9968b6e81dc970de5bdc1962c2d206edc88fde34cd31c5e4556') {
    pending.push(import('./chunks/chunk-ce51ca8957a7d29a6e2246747d399a672c8722f9f9ccf0cb83ee87fe5566edd5.js'));
  }
  if (key === '411b46b3d2650e79a00d04bc23611c30a81992cfaa958c948a9b2a0027f99b65') {
    pending.push(import('./chunks/chunk-cc98e76d9fba8f0c86d34f5781af43b87b6d5b5c1ced77fc041802272a806483.js'));
  }
  if (key === '3c00f8e5d2c4f89d8e206d899efe51d79cb7d39b511ce2405552655b015057e3') {
    pending.push(import('./chunks/chunk-7cefaa81c168f91402640106add517a5f13368c01e6b2c1ae4586e4aa6c1912d.js'));
  }
  if (key === '4b8344f950ff7d45aa299af9f26e25355bcfdcf34230d0f95d915af969319bf1') {
    pending.push(import('./chunks/chunk-cc98e76d9fba8f0c86d34f5781af43b87b6d5b5c1ced77fc041802272a806483.js'));
  }
  if (key === '2203ec0c94e2866f9fd07380ae053e92ed0199790575d875d210f31faf713d13') {
    pending.push(import('./chunks/chunk-2e0431a30f992e2e0a6446df08cb868beee6da091d6d7994751586ad45b8a324.js'));
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