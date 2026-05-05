/* ================================================
   KAVAC — Cart Page JS
   ================================================ */
function renderCart() {
  const items=Cart.get(), listEl=document.getElementById('cartList'), emptyEl=document.getElementById('emptyCart');
  if (!listEl) return;
  if (!items.length) { listEl.innerHTML=''; if(emptyEl)emptyEl.style.display='block'; updateSummary([]); return; }
  if (emptyEl) emptyEl.style.display='none';
  listEl.innerHTML = items.map(item=>`
    <div class="cart-item">
      <div class="cart-item-img">${item.icon||'🔐'}</div>
      <div class="cart-item-details">
        <div class="cart-item-name">${item.name}</div>
        <div class="cart-item-price">${formatVND(item.price)} / sản phẩm</div>
      </div>
      <div class="cart-item-controls">
        <div class="qty-ctrl">
          <button class="qty-btn" onclick="changeQty(${item.product_id},-1)">−</button>
          <span class="qty-num">${item.quantity}</span>
          <button class="qty-btn" onclick="changeQty(${item.product_id},1)">+</button>
        </div>
        <div style="font-size:0.9rem;color:var(--gold);min-width:130px;text-align:right;font-family:var(--font-display)">${formatVND(item.price*item.quantity)}</div>
        <button class="remove-btn" onclick="removeItem(${item.product_id})" title="Xóa">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/></svg>
        </button>
      </div>
    </div>`).join('');
  updateSummary(items);
}

function updateSummary(items) {
  const sub = items.reduce((s,i)=>s+i.price*i.quantity,0);
  document.getElementById('summarySubtotal').textContent = formatVND(sub);
  document.getElementById('summaryShipping').textContent = 'Miễn phí';
  document.getElementById('summaryTotal').textContent    = formatVND(sub);
  document.getElementById('summaryCount').textContent    = `${items.reduce((s,i)=>s+i.quantity,0)} sản phẩm`;
}

function changeQty(id, delta) {
  const item = Cart.get().find(i=>i.product_id===id); if (!item) return;
  if (item.quantity+delta<=0) { removeItem(id); return; }
  Cart.updateQty(id, item.quantity+delta); renderCart();
}

function removeItem(id) { Cart.remove(id); renderCart(); Toast.show('Đã xóa khỏi giỏ hàng','info'); }

function handleCheckout() {
  if (!Cart.get().length) { Toast.show('Giỏ hàng đang trống!','error'); return; }
  const user = JSON.parse(localStorage.getItem('kavac_user')||'null');
  if (!user) { Toast.show('Vui lòng đăng nhập để thanh toán','error'); setTimeout(()=>window.location.href='login.html?redirect=cart',1200); return; }
  Toast.show('Đang xử lý đơn hàng...','success');
  setTimeout(()=>{ Cart.clear(); renderCart(); Toast.show('🎉 Đặt hàng thành công! Cảm ơn bạn.','success'); },1500);
}

document.addEventListener('DOMContentLoaded',()=>{ renderCart(); document.getElementById('checkoutBtn')?.addEventListener('click',handleCheckout); });
