/* ================================================
   KAVAC — Products Page JS
   ================================================ */
const ALL_PRODUCTS = [
  {product_id:1,sku:'KV-V7-PRO',     name:'KAVAC V7 Pro',        price:7500000, category_id:1,category_name:'Tự Động Hoàn Toàn',   unlock_methods:['Vân Tay','Thẻ','Ứng Dụng','Mật Khẩu'],                  description:'Khóa cửa tự động cao cấp, tốc độ mở vân tay 0.3s, bộ nhớ 100 vân tay. Phù hợp căn hộ, villa tại TP.HCM.',icon:'🔐',stock_quantity:20},
  {product_id:2,sku:'KV-FACE-X1',    name:'KAVAC Face X1',       price:12000000,category_id:3,category_name:'Nhận Diện Khuôn Mặt',unlock_methods:['Khuôn Mặt 3D','Vân Tay','Chìa Khẩn Cấp'],               description:'Nhận diện khuôn mặt 3D, chống ảnh giả, nhận diện kể cả đeo khẩu trang. Bảo mật cấp quân sự.',icon:'👁',stock_quantity:10},
  {product_id:3,sku:'KV-PP-S1',      name:'KAVAC PushPull S1',   price:5200000, category_id:2,category_name:'Push-Pull',            unlock_methods:['Vân Tay','Mật Khẩu','Thẻ NFC'],                          description:'Thiết kế push-pull hiện đại, ergonomic, lắp đặt đơn giản trong 30 phút.',icon:'🚪',stock_quantity:35},
  {product_id:4,sku:'KV-V5-SLIM',    name:'KAVAC V5 Slim',       price:4800000, category_id:1,category_name:'Tự Động Hoàn Toàn',   unlock_methods:['Vân Tay','Mật Khẩu','Ứng Dụng'],                         description:'Phiên bản mỏng nhẹ, thiết kế tối giản, lý tưởng cho căn hộ chung cư.',icon:'🔒',stock_quantity:28},
  {product_id:5,sku:'KV-PP-PRO',     name:'KAVAC PushPull Pro',  price:6900000, category_id:2,category_name:'Push-Pull',            unlock_methods:['Vân Tay','Khuôn Mặt','Thẻ NFC','Mật Khẩu','Ứng Dụng'], description:'Dòng push-pull cao cấp nhất, 5 phương thức mở khóa, hợp kim nhôm nguyên khối.',icon:'🏠',stock_quantity:15},
  {product_id:6,sku:'KV-FACE-ULTRA', name:'KAVAC Face Ultra',    price:18500000,category_id:3,category_name:'Nhận Diện Khuôn Mặt',unlock_methods:['Khuôn Mặt 3D','Mống Mắt','Vân Tay','Ứng Dụng'],        description:'Kết hợp khuôn mặt 3D và mống mắt — đỉnh cao bảo mật sinh trắc học.',icon:'🛡',stock_quantity:5}
];

let activeCat = 'all', searchQ = '';

function renderProducts(products) {
  const grid = document.getElementById('productsGrid');
  if (!grid) return;
  if (!products.length) {
    grid.innerHTML = `<div class="no-results"><svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg><p>Không tìm thấy sản phẩm phù hợp</p></div>`;
    return;
  }
  grid.innerHTML = products.map((p,i)=>`
    <div class="product-card" style="animation-delay:${i*0.08}s" onclick="openModal(${p.product_id})">
      <div class="product-img-wrap">${p.icon}</div>
      <div class="product-info">
        <div class="product-category">${p.category_name}</div>
        <div class="product-name">${p.name}</div>
        <div class="product-methods">
          ${p.unlock_methods.slice(0,3).map(m=>`<span class="method-tag">${m}</span>`).join('')}
          ${p.unlock_methods.length>3?`<span class="method-tag">+${p.unlock_methods.length-3}</span>`:''}
        </div>
        <div class="product-footer">
          <span class="product-price">${formatVND(p.price)}</span>
          <button class="add-cart-btn" onclick="event.stopPropagation();Cart.add(ALL_PRODUCTS.find(x=>x.product_id===${p.product_id}))" title="Thêm vào giỏ">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
          </button>
        </div>
      </div>
    </div>`).join('');
}

function filterProducts() {
  let r = [...ALL_PRODUCTS];
  if (activeCat !== 'all') r = r.filter(p => p.category_id == activeCat);
  if (searchQ.trim()) { const q=searchQ.toLowerCase(); r=r.filter(p=>p.name.toLowerCase().includes(q)||p.category_name.toLowerCase().includes(q)||p.unlock_methods.some(m=>m.toLowerCase().includes(q))); }
  renderProducts(r);
}

function openModal(id) {
  const p = ALL_PRODUCTS.find(x=>x.product_id===id); if (!p) return;
  document.getElementById('modalImg').textContent      = p.icon;
  document.getElementById('modalName').textContent     = p.name;
  document.getElementById('modalCategory').textContent = p.category_name;
  document.getElementById('modalPrice').textContent    = formatVND(p.price);
  document.getElementById('modalDesc').textContent     = p.description;
  document.getElementById('modalStock').textContent    = p.stock_quantity>0?`✓ Còn ${p.stock_quantity} sản phẩm`:'✗ Hết hàng';
  document.getElementById('modalStock').style.color    = p.stock_quantity>0?'var(--success)':'var(--danger)';
  document.getElementById('modalMethods').innerHTML    = p.unlock_methods.map(m=>`<span class="method-tag">${m}</span>`).join('');
  document.getElementById('modalAddBtn').onclick       = ()=>{Cart.add(p);closeModal();};
  document.getElementById('productModal').classList.add('open');
  document.body.style.overflow = 'hidden';
}
function closeModal() { document.getElementById('productModal').classList.remove('open'); document.body.style.overflow=''; }

document.addEventListener('DOMContentLoaded', ()=>{
  document.querySelectorAll('.filter-tab').forEach(tab=>tab.addEventListener('click',()=>{
    document.querySelectorAll('.filter-tab').forEach(t=>t.classList.remove('active'));
    tab.classList.add('active'); activeCat=tab.dataset.cat; filterProducts();
  }));
  document.getElementById('productSearch')?.addEventListener('input',e=>{searchQ=e.target.value;filterProducts();});
  document.getElementById('productModal')?.addEventListener('click',e=>{if(e.target.id==='productModal')closeModal();});
  document.addEventListener('keydown',e=>{if(e.key==='Escape')closeModal();});
  const p=new URLSearchParams(window.location.search);
  const cat=p.get('cat'), pid=p.get('id');
  if(cat){activeCat=cat;document.querySelectorAll('.filter-tab').forEach(t=>t.classList.toggle('active',t.dataset.cat===cat));}
  filterProducts();
  if(pid) setTimeout(()=>openModal(parseInt(pid)),300);
});
