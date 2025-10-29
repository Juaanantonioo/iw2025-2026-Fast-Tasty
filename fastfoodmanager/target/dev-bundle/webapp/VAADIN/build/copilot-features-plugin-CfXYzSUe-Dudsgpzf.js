import { g, p as pe, w as wt, C as Co, o as oo, S as So, e as No, V as Ve, T as To, D as Dl } from "./indexhtml-CH07PwuP.js";
import { i as i$1 } from "./base-panel-Dr4zQ6S6-C063S4ad.js";
import { o } from "./icons-hMCj4mhz-BVPXX32d.js";
const x = "copilot-features-panel{padding:var(--space-100);font:var(--font-xsmall);display:grid;grid-template-columns:auto 1fr;gap:var(--space-50);height:auto}copilot-features-panel a{display:flex;align-items:center;gap:var(--space-50);white-space:nowrap}copilot-features-panel a svg{height:12px;width:12px;min-height:12px;min-width:12px}";
var y = (e, a, o2, s) => {
  for (var t = a, l = e.length - 1, i; l >= 0; l--)
    (i = e[l]) && (t = i(t) || t);
  return t;
};
const n = window.Vaadin.devTools;
let p = class extends i$1 {
  render() {
    return pe` <style>
        ${x}
      </style>
      ${g.featureFlags.map(
      (e) => pe`
          <copilot-toggle-button
            .title="${e.title}"
            ?checked=${e.enabled}
            @on-change=${(a) => this.toggleFeatureFlag(a, e)}>
          </copilot-toggle-button>
          <a class="ahreflike" href="${e.moreInfoLink}" title="Learn more" target="_blank"
            >learn more ${o.share}</a
          >
        `
    )}`;
  }
  toggleFeatureFlag(e, a) {
    const o2 = e.target.checked;
    if (wt("use-feature", { source: "toggle", enabled: o2, id: a.id }), n.frontendConnection) {
      n.frontendConnection.send("setFeature", { featureId: a.id, enabled: o2 });
      let s;
      if (a.requiresServerRestart) {
        const t = "This feature requires a server restart";
        Co() ? s = oo(
          pe`${t} <br />
              ${So()}`
        ) : s = t;
      }
      No({
        type: Ve.INFORMATION,
        message: `“${a.title}” ${o2 ? "enabled" : "disabled"}`,
        details: s,
        dismissId: `feature${a.id}${o2 ? "Enabled" : "Disabled"}`
      }), To();
    } else
      n.log("error", `Unable to toggle feature ${a.title}: No server connection available`);
  }
};
p = y([
  Dl("copilot-features-panel")
], p);
const I = {
  header: "Features",
  expanded: false,
  panelOrder: 35,
  panel: "right",
  floating: false,
  tag: "copilot-features-panel",
  helpUrl: "https://vaadin.com/docs/latest/flow/configuration/feature-flags"
}, O = {
  init(e) {
    e.addPanel(I);
  }
};
window.Vaadin.copilot.plugins.push(O);
export {
  p as CopilotFeaturesPanel
};
