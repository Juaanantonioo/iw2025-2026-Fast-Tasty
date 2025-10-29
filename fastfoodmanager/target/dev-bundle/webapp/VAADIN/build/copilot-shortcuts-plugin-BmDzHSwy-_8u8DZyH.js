import { M as Mu, D as Dl, y as y$1, O, p as pe, l as lc, X as Xu } from "./indexhtml-CH07PwuP.js";
import { i as i$1 } from "./base-panel-Dr4zQ6S6-C063S4ad.js";
import { o } from "./icons-hMCj4mhz-BVPXX32d.js";
const v = 'copilot-shortcuts-panel{display:flex;flex-direction:column;padding:var(--space-150)}copilot-shortcuts-panel h3{font:var(--font-xsmall-semibold);margin-bottom:var(--space-100);margin-top:0}copilot-shortcuts-panel h3:not(:first-of-type){margin-top:var(--space-200)}copilot-shortcuts-panel ul{display:flex;flex-direction:column;list-style:none;margin:0;padding:0}copilot-shortcuts-panel ul li{display:flex;align-items:center;gap:var(--space-50);position:relative}copilot-shortcuts-panel ul li:not(:last-of-type):before{border-bottom:1px dashed var(--border-color);content:"";inset:auto 0 0 calc(var(--size-m) + var(--space-50));position:absolute}copilot-shortcuts-panel ul li span:has(svg){align-items:center;display:flex;height:var(--size-m);justify-content:center;width:var(--size-m)}copilot-shortcuts-panel .kbds{margin-inline-start:auto}copilot-shortcuts-panel kbd{align-items:center;border:1px solid var(--border-color);border-radius:var(--radius-2);box-sizing:border-box;display:inline-flex;font-family:var(--font-family);font-size:var(--font-size-1);line-height:var(--line-height-1);padding:0 var(--space-50)}', u = window.Vaadin.copilot.tree;
if (!u)
  throw new Error("Tried to access copilot tree before it was initialized.");
var w = (t, l, h, p) => {
  for (var o2 = l, n = t.length - 1, r; n >= 0; n--)
    (r = t[n]) && (o2 = r(o2) || o2);
  return o2;
};
let d = class extends i$1 {
  constructor() {
    super(), this.onTreeUpdated = () => {
      this.requestUpdate();
    };
  }
  connectedCallback() {
    super.connectedCallback(), y$1.on("copilot-tree-created", this.onTreeUpdated);
  }
  disconnectedCallback() {
    super.disconnectedCallback(), y$1.off("copilot-tree-created", this.onTreeUpdated);
  }
  render() {
    const t = u.hasFlowComponents();
    return pe`<style>
        ${v}
      </style>
      <h3>Global</h3>
      <ul>
        <li>
          <span>${o.vaadinLogo}</span>
          <span>Copilot</span>
          ${a(Xu.toggleCopilot)}
        </li>
        <li>
          <span>${o.terminal}</span>
          <span>Command window</span>
          ${a(Xu.toggleCommandWindow)}
        </li>
        <li>
          <span>${o.flipBack}</span>
          <span>Undo</span>
          ${a(Xu.undo)}
        </li>
        <li>
          <span>${o.flipForward}</span>
          <span>Redo</span>
          ${a(Xu.redo)}
        </li>
      </ul>
      <h3>Selected component</h3>
      <ul>
        <li>
          <span>${o.fileCodeAlt}</span>
          <span>Go to source</span>
          ${a(Xu.goToSource)}
        </li>
        ${t ? pe`<li>
              <span>${o.code}</span>
              <span>Go to attach source</span>
              ${a(Xu.goToAttachSource)}
            </li>` : O}
        <li>
          <span>${o.copy}</span>
          <span>Copy</span>
          ${a(Xu.copy)}
        </li>
        <li>
          <span>${o.clipboard}</span>
          <span>Paste</span>
          ${a(Xu.paste)}
        </li>
        <li>
          <span>${o.copyAlt}</span>
          <span>Duplicate</span>
          ${a(Xu.duplicate)}
        </li>
        <li>
          <span>${o.userUp}</span>
          <span>Select parent</span>
          ${a(Xu.selectParent)}
        </li>
        <li>
          <span>${o.userLeft}</span>
          <span>Select previous sibling</span>
          ${a(Xu.selectPreviousSibling)}
        </li>
        <li>
          <span>${o.userRight}</span>
          <span>Select first child / next sibling</span>
          ${a(Xu.selectNextSibling)}
        </li>
        <li>
          <span>${o.trash}</span>
          <span>Delete</span>
          ${a(Xu.delete)}
        </li>
        <li>
          <span>${o.zap}</span>
          <span>Quick add from palette</span>
          ${a("<kbd>A ... Z</kbd>")}
        </li>
      </ul>`;
  }
};
d = w([
  Dl("copilot-shortcuts-panel")
], d);
function a(t) {
  return pe`<span class="kbds">${lc(t)}</span>`;
}
const x = Mu({
  header: "Keyboard Shortcuts",
  tag: "copilot-shortcuts-panel",
  width: 400,
  height: 550,
  floatingPosition: {
    top: 50,
    left: 50
  }
}), C = {
  init(t) {
    t.addPanel(x);
  }
};
window.Vaadin.copilot.plugins.push(C);
