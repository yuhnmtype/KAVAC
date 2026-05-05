/* ================================================
   KAVAC — Home Page JS
   ================================================ */
const MOCK_PRODUCTS = [
  {product_id:1,sku:'KV-V7-PRO',   name:'KAVAC V7 Pro',       price:7500000, category_name:'Tự Động Hoàn Toàn',   unlock_methods:['Vân Tay','Thẻ','Ứng Dụng','Mật Khẩu'],         icon:'🔐',stock_quantity:20},
  {product_id:2,sku:'KV-FACE-X1',  name:'KAVAC Face X1',      price:12000000,category_name:'Nhận Diện Khuôn Mặt',unlock_methods:['Khuôn Mặt 3D','Vân Tay','Chìa Khẩn Cấp'],      icon:'👁',stock_quantity:10},
  {product_id:3,sku:'KV-PP-S1',    name:'KAVAC PushPull S1',  price:5200000, category_name:'Push-Pull',            unlock_methods:['Vân Tay','Mật Khẩu','Thẻ NFC'],                icon:'🚪',stock_quantity:35}
];

function renderFeaturedProducts(products) {
  const grid = document.getElementById('featuredProducts');
  if (!grid) return;
  grid.innerHTML = products.map(p => `
    <div class="product-card" onclick="location.href='pages/products.html?id=${p.product_id}'">
      <div class="product-img-wrap">${p.icon}</div>
      <div class="product-info">
        <div class="product-category">${p.category_name}</div>
        <div class="product-name">${p.name}</div>
        <div class="product-methods">${p.unlock_methods.map(m=>`<span class="method-tag">${m}</span>`).join('')}</div>
        <div class="product-footer">
          <span class="product-price">${formatVND(p.price)}</span>
          <button class="add-cart-btn" onclick="event.stopPropagation();Cart.add(MOCK_PRODUCTS.find(x=>x.product_id===${p.product_id}))" title="Thêm vào giỏ">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
          </button>
        </div>
      </div>
    </div>`).join('');
}

async function loadFeaturedProducts() {
  try {
    const res = await fetch('/KAVAC-1.0-SNAPSHOT/api/products', {signal:AbortSignal.timeout(3000)});
    if (res.ok) { renderFeaturedProducts((await res.json()).slice(0,3)); return; }
  } catch {}
  await new Promise(r => setTimeout(r, 500));
  renderFeaturedProducts(MOCK_PRODUCTS);
}

document.addEventListener('DOMContentLoaded', loadFeaturedProducts);
