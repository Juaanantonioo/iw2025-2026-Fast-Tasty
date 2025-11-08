import { r as rc, y as y$1, p as pe, z as zu, c as zo, w as wt, G as Gu, g, a as se, O, d as ru, Y as Yu, W as Wu, e as No, V as Ve, D as Dl } from "./indexhtml-CeoBa05B.js";
import { b as b$1 } from "./state-Kz-UOm6S-iy6QaZKE.js";
import { i as i$1 } from "./base-panel-Dr4zQ6S6-DUuxQfsp.js";
import { o } from "./icons-hMCj4mhz-D_3KyhDJ.js";
import { S, T } from "./early-project-state-SgbtDnh2-Cadafyd9.js";
const j = 'copilot-info-panel{--dev-tools-red-color: red;--dev-tools-grey-color: gray;--dev-tools-green-color: green;position:relative}copilot-info-panel dl{margin:0;width:100%}copilot-info-panel dl>div{align-items:center;display:flex;gap:var(--space-50);height:var(--size-m);padding:0 var(--space-150);position:relative}copilot-info-panel dl>div:after{border-bottom:1px solid var(--divider-secondary-color);content:"";inset:auto var(--space-150) 0;position:absolute}copilot-info-panel dl dt{color:var(--secondary-text-color)}copilot-info-panel dl dd{align-items:center;display:flex;font-weight:var(--font-weight-medium);gap:var(--space-50);margin:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}copilot-info-panel dl dd span{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}copilot-info-panel dl dd span.icon{display:inline-flex;vertical-align:bottom}copilot-info-panel dd.live-reload-status>span{overflow:hidden;text-overflow:ellipsis;display:block;color:var(--status-color)}copilot-info-panel dd span.hidden{display:none}copilot-info-panel code{white-space:nowrap;-webkit-user-select:all;user-select:all}copilot-info-panel .checks{display:inline-grid;grid-template-columns:auto 1fr;gap:var(--space-50)}copilot-info-panel span.hint{font-size:var(--font-size-0);background:var(--gray-50);padding:var(--space-75);border-radius:var(--radius-2)}';
var J = Object.defineProperty, N = Object.getOwnPropertyDescriptor, v = (e, t, n, i) => {
  for (var o2 = i > 1 ? void 0 : i ? N(t, n) : t, s = e.length - 1, l; s >= 0; s--)
    (l = e[s]) && (o2 = (i ? l(t, n, o2) : l(o2)) || o2);
  return i && o2 && J(t, n, o2), o2;
};
let u = class extends i$1 {
  constructor() {
    super(...arguments), this.serverInfo = [], this.clientInfo = [{ name: "Browser", version: navigator.userAgent }], this.handleServerInfoEvent = (e) => {
      const t = JSON.parse(e.data.info);
      this.serverInfo = t.versions, zu().then((n) => {
        n && (this.clientInfo.unshift({ name: "Vaadin Employee", version: "true", more: void 0 }), this.requestUpdate("clientInfo"));
      }), zo() === "success" && wt("hotswap-active", { value: Gu() });
    };
  }
  connectedCallback() {
    super.connectedCallback(), this.onCommand("copilot-info", this.handleServerInfoEvent), this.onEventBus("system-info-with-callback", (e) => {
      e.detail.callback(this.getInfoForClipboard(e.detail.notify));
    }), this.reaction(
      () => g.idePluginState,
      () => {
        this.requestUpdate("serverInfo");
      }
    );
  }
  getIndex(e) {
    return this.serverInfo.findIndex((t) => t.name === e);
  }
  render() {
    const e = g.newVaadinVersionState?.versions !== void 0 && g.newVaadinVersionState.versions.length > 0, t = [...this.serverInfo, ...this.clientInfo];
    let n = this.getIndex("Spring") + 1;
    n === 0 && (n = t.length), T.springSecurityEnabled && (t.splice(n, 0, { name: "Spring Security", version: "true" }), n++), T.springJpaDataEnabled && (t.splice(n, 0, { name: "Spring Data JPA", version: "true" }), n++);
    const i = t.find((o2) => o2.name === "Vaadin");
    return i && (i.more = pe` <button
        aria-label="Edit Vaadin Version"
        class="icon relative"
        id="new-vaadin-version-btn"
        title="Edit Vaadin Version"
        @click="${(o2) => {
      o2.stopPropagation(), se.updatePanel("copilot-vaadin-versions", { floating: true });
    }}">
        ${o.editAlt}
        ${e ? pe`<span aria-hidden="true" class="absolute bg-error end-0 h-75 rounded-full top-0 w-75"></span>` : ""}
      </button>`), pe` <style>
        ${j}
      </style>
      <div class="flex flex-col gap-150 items-start">
        <dl>
          ${t.map(
      (o2) => pe`
              <div>
                <dt>${o2.name}</dt>
                <dd title="${o2.version}">
                  <span> ${this.renderValue(o2.version)} </span>
                  ${o2.more}
                </dd>
              </div>
            `
    )}
          ${this.renderDevWorkflowSection()}
        </dl>
        ${this.renderDevelopmentWorkflowButton()}
      </div>`;
  }
  renderDevWorkflowSection() {
    const e = zo(), t = this.getIdePluginLabelText(g.idePluginState), n = this.getHotswapAgentLabelText(e);
    return pe`
      <div>
        <dt>Java Hotswap</dt>
        <dd>
          ${f(e === "success", e === "success" ? "Enabled" : "Disabled")} ${n}
        </dd>
      </div>
      ${ru() !== "unsupported" ? pe` <div>
            <dt>IDE Plugin</dt>
            <dd>
              ${f(
      ru() === "success",
      ru() === "success" ? "Installed" : "Not Installed"
    )}
              ${t}
            </dd>
          </div>` : O}
    `;
  }
  renderDevelopmentWorkflowButton() {
    const e = Yu();
    let t = "", n = null, i = "";
    return e.status === "success" ? (t = "success", n = o.check, i = "Details") : e.status === "warning" ? (t = "warning", n = o.lightning, i = "Improve Development Workflow") : e.status === "error" && (t = "error", n = o.alertCircle, i = "Fix Development Workflow"), pe`
      <button
        class="mx-50"
        id="development-workflow-guide"
        @click="${() => {
      Wu();
    }}">
        <span class="prefix ${t}-text"> ${n} </span>
        ${i}
        <span class="suffix">
          <span class="bg-${t} end-0 h-75 rounded-full top-0 w-75"></span>
        </span>
      </button>
    `;
  }
  getHotswapAgentLabelText(e) {
    return e === "success" ? "Java Hotswap is enabled" : e === "error" ? "Hotswap is partially enabled" : "Hotswap is disabled";
  }
  getIdePluginLabelText(e) {
    if (ru() !== "success")
      return "Not installed";
    if (e?.version) {
      let t = null;
      return e?.ide && (e?.ide === "intellij" ? t = "IntelliJ" : e?.ide === "vscode" ? t = "VS Code" : e?.ide === "eclipse" && (t = "Eclipse")), t ? `${e?.version} ${t}` : e?.version;
    }
    return "Not installed";
  }
  renderValue(e) {
    return e === "false" ? f(false, "False") : e === "true" ? f(true, "True") : e;
  }
  getInfoForClipboard(e) {
    const t = this.renderRoot.querySelectorAll(".items-start dt"), o2 = Array.from(t).map((s) => ({
      key: s.textContent.trim(),
      value: s.nextElementSibling.textContent.trim()
    })).filter((s) => s.key !== "Live reload").filter((s) => !s.key.startsWith("Vaadin Emplo")).map((s) => {
      const { key: l } = s;
      let { value: r } = s;
      if (l === "IDE Plugin")
        r = this.getIdePluginLabelText(g.idePluginState) ?? "false";
      else if (l === "Java Hotswap") {
        const y = g.jdkInfo?.jrebel, m = zo();
        y && m === "success" ? r = "JRebel is in use" : r = this.getHotswapAgentLabelText(m);
      } else l === "Vaadin" && r.indexOf(`
`) !== -1 && (r = r.substring(0, r.indexOf(`
`)));
      return `${l}: ${r}`;
    }).join(`
`);
    return e && No({
      type: Ve.INFORMATION,
      message: "Environment information copied to clipboard",
      dismissId: "versionInfoCopied"
    }), o2.trim();
  }
};
v([
  b$1()
], u.prototype, "serverInfo", 2);
v([
  b$1()
], u.prototype, "clientInfo", 2);
u = v([
  Dl("copilot-info-panel")
], u);
let b = class extends rc {
  createRenderRoot() {
    return this;
  }
  connectedCallback() {
    super.connectedCallback(), this.style.display = "flex";
  }
  render() {
    return pe` <button
      @click=${() => {
      y$1.emit("system-info-with-callback", {
        callback: S,
        notify: true
      });
    }}
      aria-label="Copy to Clipboard"
      class="icon"
      title="Copy to Clipboard">
      <span>${o.copy}</span>
    </button>`;
  }
};
b = v([
  Dl("copilot-info-actions")
], b);
const B = {
  header: "Info",
  expanded: false,
  panelOrder: 15,
  panel: "right",
  floating: false,
  tag: "copilot-info-panel",
  actionsTag: "copilot-info-actions",
  eager: true
  // Render even when collapsed as error handling depends on this
}, W = {
  init(e) {
    e.addPanel(B);
  }
};
window.Vaadin.copilot.plugins.push(W);
function f(e, t) {
  return e ? pe`<span aria-label=${t} class="icon success-text" title=${t}>${o.check}</span>` : pe`<span aria-label=${t} class="icon error-text" title=${t}>${o.x}</span>`;
}
export {
  b as Actions,
  u as CopilotInfoPanel
};
