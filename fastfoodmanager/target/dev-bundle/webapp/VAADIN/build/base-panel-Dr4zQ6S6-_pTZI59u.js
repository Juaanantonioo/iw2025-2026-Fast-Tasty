import { r as rc, g, y as y$1 } from "./indexhtml-5mMGwTOk.js";
let i$1 = class i extends rc {
  constructor() {
    super(...arguments), this.eventBusRemovers = [], this.messageHandlers = {}, this.handleESC = (e) => {
      g.active && e.key === "Escape" && typeof this.close == "function" && this.close();
    };
  }
  createRenderRoot() {
    return this;
  }
  onEventBus(e, s) {
    this.eventBusRemovers.push(y$1.on(e, s));
  }
  connectedCallback() {
    super.connectedCallback(), this.addESCListener();
  }
  disconnectedCallback() {
    super.disconnectedCallback(), this.eventBusRemovers.forEach((e) => e()), this.removeESCListener();
  }
  addESCListener() {
    document.addEventListener("keydown", this.handleESC);
  }
  removeESCListener() {
    document.removeEventListener("keydown", this.handleESC);
  }
  onCommand(e, s) {
    this.messageHandlers[e] = s;
  }
  handleMessage(e) {
    return this.messageHandlers[e.command] ? (this.messageHandlers[e.command].call(this, e), true) : false;
  }
};
export {
  i$1 as i
};
