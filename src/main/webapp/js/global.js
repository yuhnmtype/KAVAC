/* ================================================
   KAVAC — Global JS: Theme + Cart + Toast + Navbar
   ================================================ */

// Apply theme instantly — no flash
(function(){ document.documentElement.setAttribute('data-theme', localStorage.getItem('kavac_theme')||'dark'); })();

const Theme = {
  get() { return localStorage.getItem('kavac_theme') || 'dark'; },
  apply(t) { document.documentElement.setAttribute('data-theme', t); localStorage.setItem('kavac_theme', t); },
  toggle() {
    const next = this.get() === 'dark' ? 'light' : 'dark';
    this.apply(next);
    Toast.show(next === 'dark' ? '🌙 Chế độ tối' : '☀️ Chế độ sáng', 'info');
  },
  init() {
    this.apply(this.get());
    document.querySelectorAll('.theme-toggle').forEach(btn => btn.addEventListener('click', () => Theme.toggle()));
  }
};

const Cart = {
  get()  { try { return JSON.parse(localStorage.getItem('kavac_cart')) || []; } catch { return []; } },
  save(items) { localStorage.setItem('kavac_cart', JSON.stringify(items)); Cart.updateBadge(); },
  add(product) {
    const items = Cart.get();
    const ex = items.find(i => i.product_id === product.product_id);
    if (ex) ex.quantity += 1; else items.push({...product, quantity:1});
    Cart.save(items);
    Toast.show(`✓ Đã thêm "${product.name}" vào giỏ`, 'success');
  },
  remove(id)    { Cart.save(Cart.get().filter(i => i.product_id !== id)); },
  updateQty(id, qty) {
    const items = Cart.get();
    const item = items.find(i => i.product_id === id);
    if (item) item.quantity = Math.max(1, qty);
    Cart.save(items);
  },
  clear()  { Cart.save([]); },
  total()  { return Cart.get().reduce((s,i) => s + i.price * i.quantity, 0); },
  count()  { return Cart.get().reduce((s,i) => s + i.quantity, 0); },
  updateBadge() {
    const b = document.getElementById('cartCount');
    if (!b) return;
    const n = Cart.count();
    b.textContent = n;
    b.style.display = n > 0 ? 'flex' : 'none';
  }
};

const Toast = {
  el: null, t: null,
  init() {
    if (document.getElementById('kavacToast')) { Toast.el = document.getElementById('kavacToast'); return; }
    const el = document.createElement('div');
    el.className = 'toast'; el.id = 'kavacToast';
    document.body.appendChild(el); Toast.el = el;
  },
  show(msg, type='info') {
    if (!Toast.el) Toast.init();
    Toast.el.textContent = msg;
    Toast.el.className = `toast ${type}`;
    void Toast.el.offsetWidth;
    Toast.el.classList.add('show');
    clearTimeout(Toast.t);
    Toast.t = setTimeout(() => Toast.el.classList.remove('show'), 3000);
  }
};

function initNavbar() {
  const nav = document.getElementById('navbar');
  if (!nav) return;
  const tick = () => nav.classList.toggle('scrolled', window.scrollY > 40);
  window.addEventListener('scroll', tick, {passive:true}); tick();
}

function initHamburger() {
  const btn = document.getElementById('hamburger');
  const links = document.querySelector('.nav-links');
  if (!btn || !links) return;
  btn.addEventListener('click', () => links.classList.toggle('mobile-open'));
}

function formatVND(n) {
  return new Intl.NumberFormat('vi-VN', {style:'currency', currency:'VND', maximumFractionDigits:0}).format(n);
}

function initScrollReveal() {
  const obs = new IntersectionObserver(entries => {
    entries.forEach(e => { if (e.isIntersecting) { e.target.classList.add('revealed'); obs.unobserve(e.target); } });
  }, {threshold:0.1, rootMargin:'0px 0px -40px 0px'});
  document.querySelectorAll('.feature-card,.product-card,.testimonial-card,.method').forEach(el => {
    el.style.opacity = '0'; el.style.transform = 'translateY(20px)';
    el.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
    obs.observe(el);
  });
}

document.addEventListener('DOMContentLoaded', () => {
  Toast.init(); Theme.init(); initNavbar(); initHamburger();
  Cart.updateBadge(); setTimeout(initScrollReveal, 100);
});
