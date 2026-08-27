/* ==========================================================================
   FERNWICK — main.js
   A working shop front: cart persisted to localStorage, drawer, quantity
   steppers, filters, gallery, accordions, mobile nav, toasts.
   Vanilla. No dependencies. No backend — checkout is where you wire yours in.
   ========================================================================== */

(function () {
  'use strict';

  var $ = function (s, r) { return (r || document).querySelector(s); };
  var $$ = function (s, r) { return Array.prototype.slice.call((r || document).querySelectorAll(s)); };

  var STORE_KEY = 'fernwick-cart';
  var CURRENCY = '£';

  /* ------------------------------------------------------------------------
     Cart state
     ------------------------------------------------------------------------ */

  function readCart() {
    try {
      var raw = localStorage.getItem(STORE_KEY);
      var parsed = raw ? JSON.parse(raw) : [];
      return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
      // Private mode, corrupted JSON — fall back to an empty cart rather than
      // letting the whole shop fail to boot.
      return [];
    }
  }

  function writeCart(items) {
    try { localStorage.setItem(STORE_KEY, JSON.stringify(items)); } catch (e) {}
    render();
  }

  function money(pence) {
    return CURRENCY + (pence / 100).toFixed(2);
  }

  function addItem(item) {
    var items = readCart();
    var key = item.id + '::' + (item.variant || '');
    var existing = items.filter(function (i) { return i.id + '::' + (i.variant || '') === key; })[0];

    if (existing) existing.qty += item.qty;
    else items.push(item);

    writeCart(items);
    toast(item.name + ' added to basket');
  }

  function setQty(index, qty) {
    var items = readCart();
    if (!items[index]) return;
    if (qty <= 0) items.splice(index, 1);
    else items[index].qty = qty;
    writeCart(items);
  }

  function subtotal(items) {
    return items.reduce(function (sum, i) { return sum + i.price * i.qty; }, 0);
  }

  /* ------------------------------------------------------------------------
     Render — count bubbles, drawer contents, cart page, totals
     ------------------------------------------------------------------------ */

  function render() {
    var items = readCart();
    var count = items.reduce(function (n, i) { return n + i.qty; }, 0);
    var sub = subtotal(items);
    // Free delivery over £40 — the same threshold the announcement bar states.
    var delivery = items.length === 0 || sub >= 4000 ? 0 : 495;

    $$('[data-cart-count]').forEach(function (node) {
      node.textContent = count;
      node.hidden = count === 0;
    });

    $$('[data-cart-subtotal]').forEach(function (n) { n.textContent = money(sub); });
    $$('[data-cart-delivery]').forEach(function (n) { n.textContent = delivery === 0 ? 'Free' : money(delivery); });
    $$('[data-cart-total]').forEach(function (n) { n.textContent = money(sub + delivery); });

    $$('[data-cart-empty]').forEach(function (n) { n.hidden = items.length > 0; });
    $$('[data-cart-filled]').forEach(function (n) { n.hidden = items.length === 0; });

    $$('[data-cart-lines]').forEach(function (host) {
      host.innerHTML = '';
      items.forEach(function (item, index) {
        host.appendChild(lineNode(item, index, host.dataset.cartLines === 'page'));
      });
    });
  }

  function lineNode(item, index, isPage) {
    var row = document.createElement('div');
    row.className = 'lineitem';

    var img = document.createElement('img');
    img.src = item.image;
    img.alt = '';
    img.loading = 'lazy';
    img.width = 68;
    img.height = 82;

    var mid = document.createElement('div');
    var name = document.createElement('p');
    name.className = 'lineitem__name';
    name.textContent = item.name;
    var meta = document.createElement('p');
    meta.className = 'lineitem__meta';
    meta.textContent = item.variant || '';
    mid.appendChild(name);
    mid.appendChild(meta);

    var stepper = document.createElement('div');
    stepper.className = 'qty';
    stepper.style.marginTop = 'var(--s-2)';
    stepper.style.transform = 'scale(0.86)';
    stepper.style.transformOrigin = 'left center';
    stepper.innerHTML =
      '<button type="button" aria-label="Decrease quantity">' + minusIcon() + '</button>' +
      '<input type="number" min="0" value="' + item.qty + '" aria-label="Quantity for ' + item.name + '" />' +
      '<button type="button" aria-label="Increase quantity">' + plusIcon() + '</button>';

    var buttons = $$('button', stepper);
    var input = $('input', stepper);
    buttons[0].addEventListener('click', function () { setQty(index, item.qty - 1); });
    buttons[1].addEventListener('click', function () { setQty(index, item.qty + 1); });
    input.addEventListener('change', function () { setQty(index, parseInt(input.value, 10) || 0); });
    mid.appendChild(stepper);

    var right = document.createElement('div');
    right.style.textAlign = 'right';
    var price = document.createElement('p');
    price.className = 'lineitem__price';
    price.textContent = money(item.price * item.qty);
    right.appendChild(price);

    var remove = document.createElement('button');
    remove.type = 'button';
    remove.className = 'btn btn--quiet btn--sm';
    remove.style.padding = '0';
    remove.style.height = 'auto';
    remove.style.marginTop = 'var(--s-2)';
    remove.textContent = 'Remove';
    remove.addEventListener('click', function () { setQty(index, 0); });
    right.appendChild(remove);

    row.appendChild(img);
    row.appendChild(mid);
    row.appendChild(right);
    if (isPage) row.style.paddingBottom = 'var(--s-4)';
    return row;
  }

  function plusIcon() {
    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true"><path d="M12 5v14M5 12h14"/></svg>';
  }
  function minusIcon() {
    return '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true"><path d="M5 12h14"/></svg>';
  }

  /* ------------------------------------------------------------------------
     Add-to-basket triggers
     ------------------------------------------------------------------------ */

  function initAdd() {
    $$('[data-add]').forEach(function (btn) {
      btn.addEventListener('click', function (event) {
        event.preventDefault();
        var scope = btn.closest('[data-product]') || document;
        var variantBtn = $('.variant[aria-pressed="true"]', scope);
        var qtyInput = $('[data-qty-input]', scope);

        addItem({
          id: btn.dataset.add,
          name: btn.dataset.name,
          price: parseInt(variantBtn ? variantBtn.dataset.price : btn.dataset.price, 10),
          image: btn.dataset.image,
          variant: variantBtn ? variantBtn.dataset.variant : btn.dataset.variant || '',
          qty: qtyInput ? Math.max(1, parseInt(qtyInput.value, 10) || 1) : 1
        });

        if (btn.dataset.openDrawer !== 'false') openDrawer();
      });
    });
  }

  /* ------------------------------------------------------------------------
     Cart drawer
     ------------------------------------------------------------------------ */

  var drawer, scrim;

  function openDrawer() {
    if (!drawer) return;
    drawer.dataset.open = 'true';
    if (scrim) scrim.dataset.open = 'true';
    var close = $('[data-action="close-drawer"]', drawer);
    if (close) close.focus();
  }

  function closeDrawer() {
    if (!drawer) return;
    drawer.dataset.open = 'false';
    if (scrim) scrim.dataset.open = 'false';
  }

  function initDrawer() {
    drawer = $('.drawer');
    scrim = $('#drawerScrim');
    $$('[data-action="open-drawer"]').forEach(function (b) { b.addEventListener('click', openDrawer); });
    $$('[data-action="close-drawer"]').forEach(function (b) { b.addEventListener('click', closeDrawer); });
    if (scrim) scrim.addEventListener('click', closeDrawer);
    document.addEventListener('keydown', function (e) {
      if (e.key === 'Escape' && drawer && drawer.dataset.open === 'true') closeDrawer();
    });
  }

  /* ------------------------------------------------------------------------
     Product page: variants, quantity, gallery, accordion
     ------------------------------------------------------------------------ */

  function initProduct() {
    $$('.variants').forEach(function (group) {
      var buttons = $$('.variant', group);
      buttons.forEach(function (btn) {
        btn.addEventListener('click', function () {
          buttons.forEach(function (b) { b.setAttribute('aria-pressed', String(b === btn)); });
          var priceNode = $('[data-variant-price]');
          if (priceNode) priceNode.textContent = money(parseInt(btn.dataset.price, 10));
        });
      });
    });

    $$('[data-qty]').forEach(function (stepper) {
      var input = $('input', stepper);
      var buttons = $$('button', stepper);
      buttons[0].addEventListener('click', function () {
        input.value = Math.max(1, (parseInt(input.value, 10) || 1) - 1);
      });
      buttons[1].addEventListener('click', function () {
        input.value = (parseInt(input.value, 10) || 1) + 1;
      });
    });

    var main = $('[data-gallery-main]');
    $$('[data-gallery-thumb]').forEach(function (thumb) {
      thumb.addEventListener('click', function () {
        if (!main) return;
        main.src = thumb.dataset.galleryThumb;
        main.alt = thumb.dataset.alt || '';
        $$('[data-gallery-thumb]').forEach(function (t) {
          t.setAttribute('aria-current', String(t === thumb));
        });
      });
    });

    $$('.acc__btn').forEach(function (btn) {
      btn.addEventListener('click', function () {
        var open = btn.getAttribute('aria-expanded') === 'true';
        btn.setAttribute('aria-expanded', String(!open));
        var panel = document.getElementById(btn.getAttribute('aria-controls'));
        if (panel) panel.hidden = open;
      });
    });
  }

  /* ------------------------------------------------------------------------
     Shop filters — chips, checkboxes and sort, over the rendered grid
     ------------------------------------------------------------------------ */

  function initFilters() {
    var grid = $('[data-product-grid]');
    if (!grid) return;

    var cards = $$('[data-tags]', grid);
    var countNode = $('[data-result-count]');
    var emptyNode = $('[data-no-results]');

    function active() {
      var tags = [];
      $$('.chip[aria-pressed="true"][data-filter]').forEach(function (c) { tags.push(c.dataset.filter); });
      $$('[data-filter-check]:checked').forEach(function (c) { tags.push(c.dataset.filterCheck); });
      return tags;
    }

    function apply() {
      var tags = active();
      var shown = 0;
      cards.forEach(function (card) {
        var own = card.dataset.tags.split(' ');
        var match = tags.every(function (t) { return own.indexOf(t) > -1; });
        card.hidden = !match;
        if (match) shown++;
      });
      if (countNode) countNode.textContent = shown;
      if (emptyNode) emptyNode.hidden = shown > 0;
    }

    $$('.chip[data-filter]').forEach(function (chip) {
      chip.addEventListener('click', function () {
        chip.setAttribute('aria-pressed', chip.getAttribute('aria-pressed') === 'true' ? 'false' : 'true');
        apply();
      });
    });
    $$('[data-filter-check]').forEach(function (box) { box.addEventListener('change', apply); });

    var clear = $('[data-clear-filters]');
    if (clear) clear.addEventListener('click', function () {
      $$('.chip[data-filter]').forEach(function (c) { c.setAttribute('aria-pressed', 'false'); });
      $$('[data-filter-check]').forEach(function (c) { c.checked = false; });
      apply();
    });

    var sort = $('[data-sort]');
    if (sort) sort.addEventListener('change', function () {
      var mode = sort.value;
      var sorted = cards.slice().sort(function (a, b) {
        var pa = parseInt(a.dataset.price, 10);
        var pb = parseInt(b.dataset.price, 10);
        if (mode === 'price-asc') return pa - pb;
        if (mode === 'price-desc') return pb - pa;
        if (mode === 'name') return a.dataset.name.localeCompare(b.dataset.name);
        return parseInt(a.dataset.order, 10) - parseInt(b.dataset.order, 10);
      });
      sorted.forEach(function (card) { grid.appendChild(card); });
    });

    apply();
  }

  /* ------------------------------------------------------------------------
     Wishlist, mobile nav, forms, toasts, reveal
     ------------------------------------------------------------------------ */

  function initMisc() {
    $$('.product__wish').forEach(function (btn) {
      btn.addEventListener('click', function (e) {
        e.preventDefault();
        var on = btn.getAttribute('aria-pressed') === 'true';
        btn.setAttribute('aria-pressed', String(!on));
        toast(on ? 'Removed from wishlist' : 'Saved to wishlist');
      });
    });

    var burger = $('.burger');
    var nav = $('.shopnav');
    if (burger && nav) {
      burger.addEventListener('click', function () {
        var open = nav.dataset.open === 'true';
        nav.dataset.open = String(!open);
        burger.setAttribute('aria-expanded', String(!open));
      });
    }

    $$('form[data-demo-form]').forEach(function (form) {
      form.addEventListener('submit', function (e) {
        e.preventDefault();
        toast(form.dataset.demoForm);
        form.reset();
      });
    });

    $$('[data-toast]').forEach(function (btn) {
      btn.addEventListener('click', function () { toast(btn.dataset.toast); });
    });
  }

  function toast(message) {
    var host = $('.toasts');
    if (!host) return;
    var node = document.createElement('div');
    node.className = 'toast';
    node.setAttribute('role', 'status');
    node.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m20 6-11 11-5-5"/></svg><span></span>';
    $('span', node).textContent = message;
    host.appendChild(node);
    setTimeout(function () {
      node.style.transition = 'opacity 220ms';
      node.style.opacity = '0';
      setTimeout(function () { node.remove(); }, 240);
    }, 2400);
  }

  function initReveal() {
    var targets = $$('[data-reveal]');
    if (!targets.length) return;
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches || !('IntersectionObserver' in window)) {
      targets.forEach(function (t) { t.dataset.shown = 'true'; });
      return;
    }
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (!entry.isIntersecting) return;
        entry.target.dataset.shown = 'true';
        io.unobserve(entry.target);
      });
    }, { rootMargin: '0px 0px -8% 0px', threshold: 0.05 });
    targets.forEach(function (t) { io.observe(t); });
  }

  /* ---------------------------------------------------------------------- */
  document.addEventListener('DOMContentLoaded', function () {
    initDrawer();
    initAdd();
    initProduct();
    initFilters();
    initMisc();
    initReveal();
    render();
  });

  window.Fernwick = { toast: toast, addItem: addItem, readCart: readCart };
})();
