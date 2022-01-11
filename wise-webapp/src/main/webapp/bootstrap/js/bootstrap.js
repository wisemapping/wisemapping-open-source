/*!
 * Bootstrap v3.1.1 (http://getbootstrap.com)
 * Copyright 2011-2014 Twitter, Inc.
 * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 */

if (typeof jQuery === 'undefined') { throw new Error('Bootstrap\'s JavaScript requires jQuery'); }

/* ========================================================================
 * Bootstrap: transition.js v3.1.1
 * http://getbootstrap.com/javascript/#transitions
 * ========================================================================
 * Copyright 2011-2014 Twitter, Inc.
 * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ======================================================================== */

+(function ($) {
  // CSS TRANSITION SUPPORT (Shoutout: http://www.modernizr.com/)
  // ============================================================

  function transitionEnd() {
    const el = document.createElement('bootstrap');

    const transEndEventNames = {
      WebkitTransition: 'webkitTransitionEnd',
      MozTransition: 'transitionend',
      OTransition: 'oTransitionEnd otransitionend',
      transition: 'transitionend',
    };

    for (const name in transEndEventNames) {
      if (el.style[name] !== undefined) {
        return { end: transEndEventNames[name] };
      }
    }

    return false; // explicit for ie8 (  ._.)
  }

  // http://blog.alexmaccaw.com/css-transitions
  $.fn.emulateTransitionEnd = function (duration) {
    let called = false; const
      $el = this;
    $(this).one($.support.transition.end, () => { called = true; });
    const callback = function () { if (!called) $($el).trigger($.support.transition.end); };
    setTimeout(callback, duration);
    return this;
  };

  $(() => {
    $.support.transition = transitionEnd();
  });
}(jQuery));

/* ========================================================================
 * Bootstrap: alert.js v3.1.1
 * http://getbootstrap.com/javascript/#alerts
 * ========================================================================
 * Copyright 2011-2014 Twitter, Inc.
 * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ======================================================================== */

+(function ($) {
  // ALERT CLASS DEFINITION
  // ======================

  const dismiss = '[data-dismiss="alert"]';
  const Alert = function (el) {
    $(el).on('click', dismiss, this.close);
  };

  Alert.prototype.close = function (e) {
    const $this = $(this);
    let selector = $this.attr('data-target');

    if (!selector) {
      selector = $this.attr('href');
      selector = selector && selector.replace(/.*(?=#[^\s]*$)/, ''); // strip for ie7
    }

    let $parent = $(selector);

    if (e) e.preventDefault();

    if (!$parent.length) {
      $parent = $this.hasClass('alert') ? $this : $this.parent();
    }

    $parent.trigger(e = $.Event('close.bs.alert'));

    if (e.isDefaultPrevented()) return;

    $parent.removeClass('in');

    function removeElement() {
      $parent.trigger('closed.bs.alert').remove();
    }

    $.support.transition && $parent.hasClass('fade')
      ? $parent
        .one($.support.transition.end, removeElement)
        .emulateTransitionEnd(150)
      : removeElement();
  };

  // ALERT PLUGIN DEFINITION
  // =======================

  const old = $.fn.alert;

  $.fn.alert = function (option) {
    return this.each(function () {
      const $this = $(this);
      let data = $this.data('bs.alert');

      if (!data) $this.data('bs.alert', (data = new Alert(this)));
      if (typeof option === 'string') data[option].call($this);
    });
  };

  $.fn.alert.Constructor = Alert;

  // ALERT NO CONFLICT
  // =================

  $.fn.alert.noConflict = function () {
    $.fn.alert = old;
    return this;
  };

  // ALERT DATA-API
  // ==============

  $(document).on('click.bs.alert.data-api', dismiss, Alert.prototype.close);
}(jQuery));

/* ========================================================================
 * Bootstrap: button.js v3.1.1
 * http://getbootstrap.com/javascript/#buttons
 * ========================================================================
 * Copyright 2011-2014 Twitter, Inc.
 * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ======================================================================== */

+(function ($) {
  // BUTTON PUBLIC CLASS DEFINITION
  // ==============================

  var Button = function (element, options) {
    this.$element = $(element);
    this.options = $.extend({}, Button.DEFAULTS, options);
    this.isLoading = false;
  };

  Button.DEFAULTS = {
    loadingText: 'loading...',
  };

  Button.prototype.setState = function (state) {
    const d = 'disabled';
    const $el = this.$element;
    const val = $el.is('input') ? 'val' : 'html';
    const data = $el.data();

    state += 'Text';

    if (!data.resetText) $el.data('resetText', $el[val]());

    $el[val](data[state] || this.options[state]);

    // push to event loop to allow forms to submit
    setTimeout($.proxy(function () {
      if (state == 'loadingText') {
        this.isLoading = true;
        $el.addClass(d).attr(d, d);
      } else if (this.isLoading) {
        this.isLoading = false;
        $el.removeClass(d).removeAttr(d);
      }
    }, this), 0);
  };

  Button.prototype.toggle = function () {
    let changed = true;
    const $parent = this.$element.closest('[data-toggle="buttons"]');

    if ($parent.length) {
      const $input = this.$element.find('input');
      if ($input.prop('type') == 'radio') {
        if ($input.prop('checked') && this.$element.hasClass('active')) changed = false;
        else $parent.find('.active').removeClass('active');
      }
      if (changed) $input.prop('checked', !this.$element.hasClass('active')).trigger('change');
    }

    if (changed) this.$element.toggleClass('active');
  };

  // BUTTON PLUGIN DEFINITION
  // ========================

  const old = $.fn.button;

  $.fn.button = function (option) {
    return this.each(function () {
      const $this = $(this);
      let data = $this.data('bs.button');
      const options = typeof option === 'object' && option;

      if (!data) $this.data('bs.button', (data = new Button(this, options)));

      if (option == 'toggle') data.toggle();
      else if (option) data.setState(option);
    });
  };

  $.fn.button.Constructor = Button;

  // BUTTON NO CONFLICT
  // ==================

  $.fn.button.noConflict = function () {
    $.fn.button = old;
    return this;
  };

  // BUTTON DATA-API
  // ===============

  $(document).on('click.bs.button.data-api', '[data-toggle^=button]', (e) => {
    let $btn = $(e.target);
    if (!$btn.hasClass('btn')) $btn = $btn.closest('.btn');
    $btn.button('toggle');
    e.preventDefault();
  });
}(jQuery));

/* ========================================================================
 * Bootstrap: carousel.js v3.1.1
 * http://getbootstrap.com/javascript/#carousel
 * ========================================================================
 * Copyright 2011-2014 Twitter, Inc.
 * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ======================================================================== */

+(function ($) {
  // CAROUSEL CLASS DEFINITION
  // =========================

  const Carousel = function (element, options) {
    this.$element = $(element);
    this.$indicators = this.$element.find('.carousel-indicators');
    this.options = options;
    this.paused = this.sliding = this.interval = this.$active = this.$items = null;

    this.options.pause == 'hover' && this.$element
      .on('mouseenter', $.proxy(this.pause, this))
      .on('mouseleave', $.proxy(this.cycle, this));
  };

  Carousel.DEFAULTS = {
    interval: 5000,
    pause: 'hover',
    wrap: true,
  };

  Carousel.prototype.cycle = function (e) {
    e || (this.paused = false);

    this.interval && clearInterval(this.interval);

    this.options.interval
      && !this.paused
      && (this.interval = setInterval($.proxy(this.next, this), this.options.interval));

    return this;
  };

  Carousel.prototype.getActiveIndex = function () {
    this.$active = this.$element.find('.item.active');
    this.$items = this.$active.parent().children();

    return this.$items.index(this.$active);
  };

  Carousel.prototype.to = function (pos) {
    const that = this;
    const activeIndex = this.getActiveIndex();

    if (pos > (this.$items.length - 1) || pos < 0) return;

    if (this.sliding) return this.$element.one('slid.bs.carousel', () => { that.to(pos); });
    if (activeIndex == pos) return this.pause().cycle();

    return this.slide(pos > activeIndex ? 'next' : 'prev', $(this.$items[pos]));
  };

  Carousel.prototype.pause = function (e) {
    e || (this.paused = true);

    if (this.$element.find('.next, .prev').length && $.support.transition) {
      this.$element.trigger($.support.transition.end);
      this.cycle(true);
    }

    this.interval = clearInterval(this.interval);

    return this;
  };

  Carousel.prototype.next = function () {
    if (this.sliding) return;
    return this.slide('next');
  };

  Carousel.prototype.prev = function () {
    if (this.sliding) return;
    return this.slide('prev');
  };

  Carousel.prototype.slide = function (type, next) {
    const $active = this.$element.find('.item.active');
    let $next = next || $active[type]();
    const isCycling = this.interval;
    const direction = type == 'next' ? 'left' : 'right';
    const fallback = type == 'next' ? 'first' : 'last';
    const that = this;

    if (!$next.length) {
      if (!this.options.wrap) return;
      $next = this.$element.find('.item')[fallback]();
    }

    if ($next.hasClass('active')) return this.sliding = false;

    const e = $.Event('slide.bs.carousel', { relatedTarget: $next[0], direction });
    this.$element.trigger(e);
    if (e.isDefaultPrevented()) return;

    this.sliding = true;

    isCycling && this.pause();

    if (this.$indicators.length) {
      this.$indicators.find('.active').removeClass('active');
      this.$element.one('slid.bs.carousel', () => {
        const $nextIndicator = $(that.$indicators.children()[that.getActiveIndex()]);
        $nextIndicator && $nextIndicator.addClass('active');
      });
    }

    if ($.support.transition && this.$element.hasClass('slide')) {
      $next.addClass(type);
      $next[0].offsetWidth; // force reflow
      $active.addClass(direction);
      $next.addClass(direction);
      $active
        .one($.support.transition.end, () => {
          $next.removeClass([type, direction].join(' ')).addClass('active');
          $active.removeClass(['active', direction].join(' '));
          that.sliding = false;
          setTimeout(() => { that.$element.trigger('slid.bs.carousel'); }, 0);
        })
        .emulateTransitionEnd($active.css('transition-duration').slice(0, -1) * 1000);
    } else {
      $active.removeClass('active');
      $next.addClass('active');
      this.sliding = false;
      this.$element.trigger('slid.bs.carousel');
    }

    isCycling && this.cycle();

    return this;
  };

  // CAROUSEL PLUGIN DEFINITION
  // ==========================

  const old = $.fn.carousel;

  $.fn.carousel = function (option) {
    return this.each(function () {
      const $this = $(this);
      let data = $this.data('bs.carousel');
      const options = $.extend({}, Carousel.DEFAULTS, $this.data(), typeof option === 'object' && option);
      const action = typeof option === 'string' ? option : options.slide;

      if (!data) $this.data('bs.carousel', (data = new Carousel(this, options)));
      if (typeof option === 'number') data.to(option);
      else if (action) data[action]();
      else if (options.interval) data.pause().cycle();
    });
  };

  $.fn.carousel.Constructor = Carousel;

  // CAROUSEL NO CONFLICT
  // ====================

  $.fn.carousel.noConflict = function () {
    $.fn.carousel = old;
    return this;
  };

  // CAROUSEL DATA-API
  // =================

  $(document).on('click.bs.carousel.data-api', '[data-slide], [data-slide-to]', function (e) {
    const $this = $(this); let
      href;
    const $target = $($this.attr('data-target') || (href = $this.attr('href')) && href.replace(/.*(?=#[^\s]+$)/, '')); // strip for ie7
    const options = $.extend({}, $target.data(), $this.data());
    let slideIndex = $this.attr('data-slide-to');
    if (slideIndex) options.interval = false;

    $target.carousel(options);

    if (slideIndex = $this.attr('data-slide-to')) {
      $target.data('bs.carousel').to(slideIndex);
    }

    e.preventDefault();
  });

  $(window).on('load', () => {
    $('[data-ride="carousel"]').each(function () {
      const $carousel = $(this);
      $carousel.carousel($carousel.data());
    });
  });
}(jQuery));

/* ========================================================================
 * Bootstrap: collapse.js v3.1.1
 * http://getbootstrap.com/javascript/#collapse
 * ========================================================================
 * Copyright 2011-2014 Twitter, Inc.
 * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ======================================================================== */

+(function ($) {
  // COLLAPSE PUBLIC CLASS DEFINITION
  // ================================

  var Collapse = function (element, options) {
    this.$element = $(element);
    this.options = $.extend({}, Collapse.DEFAULTS, options);
    this.transitioning = null;

    if (this.options.parent) this.$parent = $(this.options.parent);
    if (this.options.toggle) this.toggle();
  };

  Collapse.DEFAULTS = {
    toggle: true,
  };

  Collapse.prototype.dimension = function () {
    const hasWidth = this.$element.hasClass('width');
    return hasWidth ? 'width' : 'height';
  };

  Collapse.prototype.show = function () {
    if (this.transitioning || this.$element.hasClass('in')) return;

    const startEvent = $.Event('show.bs.collapse');
    this.$element.trigger(startEvent);
    if (startEvent.isDefaultPrevented()) return;

    const actives = this.$parent && this.$parent.find('> .panel > .in');

    if (actives && actives.length) {
      const hasData = actives.data('bs.collapse');
      if (hasData && hasData.transitioning) return;
      actives.collapse('hide');
      hasData || actives.data('bs.collapse', null);
    }

    const dimension = this.dimension();

    this.$element
      .removeClass('collapse')
      .addClass('collapsing')
      [dimension](0);

    this.transitioning = 1;

    const complete = function () {
      this.$element
        .removeClass('collapsing')
        .addClass('collapse in')
        [dimension]('auto');
      this.transitioning = 0;
      this.$element.trigger('shown.bs.collapse');
    };

    if (!$.support.transition) return complete.call(this);

    const scrollSize = $.camelCase(['scroll', dimension].join('-'));

    this.$element
      .one($.support.transition.end, $.proxy(complete, this))
      .emulateTransitionEnd(350)
      [dimension](this.$element[0][scrollSize]);
  };

  Collapse.prototype.hide = function () {
    if (this.transitioning || !this.$element.hasClass('in')) return;

    const startEvent = $.Event('hide.bs.collapse');
    this.$element.trigger(startEvent);
    if (startEvent.isDefaultPrevented()) return;

    const dimension = this.dimension();

    this.$element
      [dimension](this.$element[dimension]())
      [0].offsetHeight;

    this.$element
      .addClass('collapsing')
      .removeClass('collapse')
      .removeClass('in');

    this.transitioning = 1;

    const complete = function () {
      this.transitioning = 0;
      this.$element
        .trigger('hidden.bs.collapse')
        .removeClass('collapsing')
        .addClass('collapse');
    };

    if (!$.support.transition) return complete.call(this);

    this.$element
      [dimension](0)
      .one($.support.transition.end, $.proxy(complete, this))
      .emulateTransitionEnd(350);
  };

  Collapse.prototype.toggle = function () {
    this[this.$element.hasClass('in') ? 'hide' : 'show']();
  };

  // COLLAPSE PLUGIN DEFINITION
  // ==========================

  const old = $.fn.collapse;

  $.fn.collapse = function (option) {
    return this.each(function () {
      const $this = $(this);
      let data = $this.data('bs.collapse');
      const options = $.extend({}, Collapse.DEFAULTS, $this.data(), typeof option === 'object' && option);

      if (!data && options.toggle && option == 'show') option = !option;
      if (!data) $this.data('bs.collapse', (data = new Collapse(this, options)));
      if (typeof option === 'string') data[option]();
    });
  };

  $.fn.collapse.Constructor = Collapse;

  // COLLAPSE NO CONFLICT
  // ====================

  $.fn.collapse.noConflict = function () {
    $.fn.collapse = old;
    return this;
  };

  // COLLAPSE DATA-API
  // =================

  $(document).on('click.bs.collapse.data-api', '[data-toggle=collapse]', function (e) {
    const $this = $(this); let
      href;
    const target = $this.attr('data-target')
        || e.preventDefault()
        || (href = $this.attr('href')) && href.replace(/.*(?=#[^\s]+$)/, ''); // strip for ie7
    const $target = $(target);
    const data = $target.data('bs.collapse');
    const option = data ? 'toggle' : $this.data();
    const parent = $this.attr('data-parent');
    const $parent = parent && $(parent);

    if (!data || !data.transitioning) {
      if ($parent) $parent.find(`[data-toggle=collapse][data-parent="${parent}"]`).not($this).addClass('collapsed');
      $this[$target.hasClass('in') ? 'addClass' : 'removeClass']('collapsed');
    }

    $target.collapse(option);
  });
}(jQuery));

/* ========================================================================
 * Bootstrap: dropdown.js v3.1.1
 * http://getbootstrap.com/javascript/#dropdowns
 * ========================================================================
 * Copyright 2011-2014 Twitter, Inc.
 * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ======================================================================== */

+(function ($) {
  // DROPDOWN CLASS DEFINITION
  // =========================

  const backdrop = '.dropdown-backdrop';
  const toggle = '[data-toggle=dropdown]';
  const Dropdown = function (element) {
    $(element).on('click.bs.dropdown', this.toggle);
  };

  Dropdown.prototype.toggle = function (e) {
    const $this = $(this);

    if ($this.is('.disabled, :disabled')) return;

    const $parent = getParent($this);
    const isActive = $parent.hasClass('open');

    clearMenus();

    if (!isActive) {
      if ('ontouchstart' in document.documentElement && !$parent.closest('.navbar-nav').length) {
        // if mobile we use a backdrop because click events don't delegate
        $('<div class="dropdown-backdrop"/>').insertAfter($(this)).on('click', clearMenus);
      }

      const relatedTarget = { relatedTarget: this };
      $parent.trigger(e = $.Event('show.bs.dropdown', relatedTarget));

      if (e.isDefaultPrevented()) return;

      $parent
        .toggleClass('open')
        .trigger('shown.bs.dropdown', relatedTarget);

      $this.focus();
    }

    return false;
  };

  Dropdown.prototype.keydown = function (e) {
    if (!/(38|40|27)/.test(e.keyCode)) return;

    const $this = $(this);

    e.preventDefault();
    e.stopPropagation();

    if ($this.is('.disabled, :disabled')) return;

    const $parent = getParent($this);
    const isActive = $parent.hasClass('open');

    if (!isActive || (isActive && e.keyCode == 27)) {
      if (e.which == 27) $parent.find(toggle).focus();
      return $this.click();
    }

    const desc = ' li:not(.divider):visible a';
    const $items = $parent.find(`[role=menu]${desc}, [role=listbox]${desc}`);

    if (!$items.length) return;

    let index = $items.index($items.filter(':focus'));

    if (e.keyCode == 38 && index > 0) index--; // up
    if (e.keyCode == 40 && index < $items.length - 1) index++; // down
    if (!~index) index = 0;

    $items.eq(index).focus();
  };

  function clearMenus(e) {
    $(backdrop).remove();
    $(toggle).each(function () {
      const $parent = getParent($(this));
      const relatedTarget = { relatedTarget: this };
      if (!$parent.hasClass('open')) return;
      $parent.trigger(e = $.Event('hide.bs.dropdown', relatedTarget));
      if (e.isDefaultPrevented()) return;
      $parent.removeClass('open').trigger('hidden.bs.dropdown', relatedTarget);
    });
  }

  function getParent($this) {
    let selector = $this.attr('data-target');

    if (!selector) {
      selector = $this.attr('href');
      selector = selector && /#[A-Za-z]/.test(selector) && selector.replace(/.*(?=#[^\s]*$)/, ''); // strip for ie7
    }

    const $parent = selector && $(selector);

    return $parent && $parent.length ? $parent : $this.parent();
  }

  // DROPDOWN PLUGIN DEFINITION
  // ==========================

  const old = $.fn.dropdown;

  $.fn.dropdown = function (option) {
    return this.each(function () {
      const $this = $(this);
      let data = $this.data('bs.dropdown');

      if (!data) $this.data('bs.dropdown', (data = new Dropdown(this)));
      if (typeof option === 'string') data[option].call($this);
    });
  };

  $.fn.dropdown.Constructor = Dropdown;

  // DROPDOWN NO CONFLICT
  // ====================

  $.fn.dropdown.noConflict = function () {
    $.fn.dropdown = old;
    return this;
  };

  // APPLY TO STANDARD DROPDOWN ELEMENTS
  // ===================================

  $(document)
    .on('click.bs.dropdown.data-api', clearMenus)
    .on('click.bs.dropdown.data-api', '.dropdown form', (e) => { e.stopPropagation(); })
    .on('click.bs.dropdown.data-api', toggle, Dropdown.prototype.toggle)
    .on('keydown.bs.dropdown.data-api', `${toggle}, [role=menu], [role=listbox]`, Dropdown.prototype.keydown);
}(jQuery));

/* ========================================================================
 * Bootstrap: modal.js v3.1.1
 * http://getbootstrap.com/javascript/#modals
 * ========================================================================
 * Copyright 2011-2014 Twitter, Inc.
 * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ======================================================================== */

+(function ($) {
  // MODAL CLASS DEFINITION
  // ======================

  const Modal = function (element, options) {
    this.options = options;
    this.$element = $(element);
    this.$backdrop = this.isShown = null;

    if (this.options.remote) {
      this.$element
        .find('.modal-content')
        .load(this.options.remote, $.proxy(function () {
          this.$element.trigger('loaded.bs.modal');
        }, this));
    }
  };

  Modal.DEFAULTS = {
    backdrop: true,
    keyboard: true,
    show: true,
  };

  Modal.prototype.toggle = function (_relatedTarget) {
    return this[!this.isShown ? 'show' : 'hide'](_relatedTarget);
  };

  Modal.prototype.show = function (_relatedTarget) {
    const that = this;
    const e = $.Event('show.bs.modal', { relatedTarget: _relatedTarget });

    this.$element.trigger(e);

    if (this.isShown || e.isDefaultPrevented()) return;

    this.isShown = true;

    this.escape();

    this.$element.on('click.dismiss.bs.modal', '[data-dismiss="modal"]', $.proxy(this.hide, this));

    this.backdrop(() => {
      const transition = $.support.transition && that.$element.hasClass('fade');

      if (!that.$element.parent().length) {
        that.$element.appendTo(document.body); // don't move modals dom position
      }

      that.$element
        .show()
        .scrollTop(0);

      if (transition) {
        that.$element[0].offsetWidth; // force reflow
      }

      that.$element
        .addClass('in')
        .attr('aria-hidden', false);

      that.enforceFocus();

      const e = $.Event('shown.bs.modal', { relatedTarget: _relatedTarget });

      transition
        ? that.$element.find('.modal-dialog') // wait for modal to slide in
          .one($.support.transition.end, () => {
            that.$element.focus().trigger(e);
          })
          .emulateTransitionEnd(300)
        : that.$element.focus().trigger(e);
    });
  };

  Modal.prototype.hide = function (e) {
    if (e) e.preventDefault();

    e = $.Event('hide.bs.modal');

    this.$element.trigger(e);

    if (!this.isShown || e.isDefaultPrevented()) return;

    this.isShown = false;

    this.escape();

    $(document).off('focusin.bs.modal');

    this.$element
      .removeClass('in')
      .attr('aria-hidden', true)
      .off('click.dismiss.bs.modal');

    $.support.transition && this.$element.hasClass('fade')
      ? this.$element
        .one($.support.transition.end, $.proxy(this.hideModal, this))
        .emulateTransitionEnd(300)
      : this.hideModal();
  };

  Modal.prototype.enforceFocus = function () {
    $(document)
      .off('focusin.bs.modal') // guard against infinite focus loop
      .on('focusin.bs.modal', $.proxy(function (e) {
        if (this.$element[0] !== e.target && !this.$element.has(e.target).length) {
          this.$element.focus();
        }
      }, this));
  };

  Modal.prototype.escape = function () {
    if (this.isShown && this.options.keyboard) {
      this.$element.on('keyup.dismiss.bs.modal', $.proxy(function (e) {
        e.which == 27 && this.hide();
      }, this));
    } else if (!this.isShown) {
      this.$element.off('keyup.dismiss.bs.modal');
    }
  };

  Modal.prototype.hideModal = function () {
    const that = this;
    this.$element.hide();
    this.backdrop(() => {
      that.removeBackdrop();
      that.$element.trigger('hidden.bs.modal');
    });
  };

  Modal.prototype.removeBackdrop = function () {
    this.$backdrop && this.$backdrop.remove();
    this.$backdrop = null;
  };

  Modal.prototype.backdrop = function (callback) {
    const animate = this.$element.hasClass('fade') ? 'fade' : '';

    if (this.isShown && this.options.backdrop) {
      const doAnimate = $.support.transition && animate;

      this.$backdrop = $(`<div class="modal-backdrop ${animate}" />`)
        .appendTo(document.body);

      this.$element.on('click.dismiss.bs.modal', $.proxy(function (e) {
        if (e.target !== e.currentTarget) return;
        this.options.backdrop == 'static'
          ? this.$element[0].focus.call(this.$element[0])
          : this.hide.call(this);
      }, this));

      if (doAnimate) this.$backdrop[0].offsetWidth; // force reflow

      this.$backdrop.addClass('in');

      if (!callback) return;

      doAnimate
        ? this.$backdrop
          .one($.support.transition.end, callback)
          .emulateTransitionEnd(150)
        : callback();
    } else if (!this.isShown && this.$backdrop) {
      this.$backdrop.removeClass('in');

      $.support.transition && this.$element.hasClass('fade')
        ? this.$backdrop
          .one($.support.transition.end, callback)
          .emulateTransitionEnd(150)
        : callback();
    } else if (callback) {
      callback();
    }
  };

  // MODAL PLUGIN DEFINITION
  // =======================

  const old = $.fn.modal;

  $.fn.modal = function (option, _relatedTarget) {
    return this.each(function () {
      const $this = $(this);
      let data = $this.data('bs.modal');
      const options = $.extend({}, Modal.DEFAULTS, $this.data(), typeof option === 'object' && option);

      if (!data) $this.data('bs.modal', (data = new Modal(this, options)));
      if (typeof option === 'string') data[option](_relatedTarget);
      else if (options.show) data.show(_relatedTarget);
    });
  };

  $.fn.modal.Constructor = Modal;

  // MODAL NO CONFLICT
  // =================

  $.fn.modal.noConflict = function () {
    $.fn.modal = old;
    return this;
  };

  // MODAL DATA-API
  // ==============

  $(document).on('click.bs.modal.data-api', '[data-toggle="modal"]', function (e) {
    const $this = $(this);
    const href = $this.attr('href');
    const $target = $($this.attr('data-target') || (href && href.replace(/.*(?=#[^\s]+$)/, ''))); // strip for ie7
    const option = $target.data('bs.modal') ? 'toggle' : $.extend({ remote: !/#/.test(href) && href }, $target.data(), $this.data());

    if ($this.is('a')) e.preventDefault();

    $target
      .modal(option, this)
      .one('hide', () => {
        $this.is(':visible') && $this.focus();
      });
  });

  $(document)
    .on('show.bs.modal', '.modal', () => { $(document.body).addClass('modal-open'); })
    .on('hidden.bs.modal', '.modal', () => { $(document.body).removeClass('modal-open'); });
}(jQuery));

/* ========================================================================
 * Bootstrap: tooltip.js v3.1.1
 * http://getbootstrap.com/javascript/#tooltip
 * Inspired by the original jQuery.tipsy by Jason Frame
 * ========================================================================
 * Copyright 2011-2014 Twitter, Inc.
 * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ======================================================================== */

+(function ($) {
  // TOOLTIP PUBLIC CLASS DEFINITION
  // ===============================

  const Tooltip = function (element, options) {
    this.type = this.options = this.enabled = this.timeout = this.hoverState = this.$element = null;

    this.init('tooltip', element, options);
  };

  Tooltip.DEFAULTS = {
    animation: true,
    placement: 'top',
    selector: false,
    template: '<div class="tooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>',
    trigger: 'hover focus',
    title: '',
    delay: 0,
    html: false,
    container: false,
  };

  Tooltip.prototype.init = function (type, element, options) {
    this.enabled = true;
    this.type = type;
    this.$element = $(element);
    this.options = this.getOptions(options);

    const triggers = this.options.trigger.split(' ');

    for (let i = triggers.length; i--;) {
      const trigger = triggers[i];

      if (trigger == 'click') {
        this.$element.on(`click.${this.type}`, this.options.selector, $.proxy(this.toggle, this));
      } else if (trigger != 'manual') {
        const eventIn = trigger == 'hover' ? 'mouseenter' : 'focusin';
        const eventOut = trigger == 'hover' ? 'mouseleave' : 'focusout';

        this.$element.on(`${eventIn}.${this.type}`, this.options.selector, $.proxy(this.enter, this));
        this.$element.on(`${eventOut}.${this.type}`, this.options.selector, $.proxy(this.leave, this));
      }
    }

    this.options.selector
      ? (this._options = $.extend({}, this.options, { trigger: 'manual', selector: '' }))
      : this.fixTitle();
  };

  Tooltip.prototype.getDefaults = function () {
    return Tooltip.DEFAULTS;
  };

  Tooltip.prototype.getOptions = function (options) {
    options = $.extend({}, this.getDefaults(), this.$element.data(), options);

    if (options.delay && typeof options.delay === 'number') {
      options.delay = {
        show: options.delay,
        hide: options.delay,
      };
    }

    return options;
  };

  Tooltip.prototype.getDelegateOptions = function () {
    const options = {};
    const defaults = this.getDefaults();

    this._options && $.each(this._options, (key, value) => {
      if (defaults[key] != value) options[key] = value;
    });

    return options;
  };

  Tooltip.prototype.enter = function (obj) {
    const self = obj instanceof this.constructor
      ? obj : $(obj.currentTarget)[this.type](this.getDelegateOptions()).data(`bs.${this.type}`);

    clearTimeout(self.timeout);

    self.hoverState = 'in';

    if (!self.options.delay || !self.options.delay.show) return self.show();

    self.timeout = setTimeout(() => {
      if (self.hoverState == 'in') self.show();
    }, self.options.delay.show);
  };

  Tooltip.prototype.leave = function (obj) {
    const self = obj instanceof this.constructor
      ? obj : $(obj.currentTarget)[this.type](this.getDelegateOptions()).data(`bs.${this.type}`);

    clearTimeout(self.timeout);

    self.hoverState = 'out';

    if (!self.options.delay || !self.options.delay.hide) return self.hide();

    self.timeout = setTimeout(() => {
      if (self.hoverState == 'out') self.hide();
    }, self.options.delay.hide);
  };

  Tooltip.prototype.show = function () {
    const e = $.Event(`show.bs.${this.type}`);

    if (this.hasContent() && this.enabled) {
      this.$element.trigger(e);

      if (e.isDefaultPrevented()) return;
      const that = this;

      const $tip = this.tip();

      this.setContent();

      if (this.options.animation) $tip.addClass('fade');

      let placement = typeof this.options.placement === 'function'
        ? this.options.placement.call(this, $tip[0], this.$element[0])
        : this.options.placement;

      const autoToken = /\s?auto?\s?/i;
      const autoPlace = autoToken.test(placement);
      if (autoPlace) placement = placement.replace(autoToken, '') || 'top';

      $tip
        .detach()
        .css({ top: 0, left: 0, display: 'block' })
        .addClass(placement);

      this.options.container ? $tip.appendTo(this.options.container) : $tip.insertAfter(this.$element);

      const pos = this.getPosition();
      const actualWidth = $tip[0].offsetWidth;
      const actualHeight = $tip[0].offsetHeight;

      if (autoPlace) {
        const $parent = this.$element.parent();

        const orgPlacement = placement;
        const docScroll = document.documentElement.scrollTop || document.body.scrollTop;
        const parentWidth = this.options.container == 'body' ? window.innerWidth : $parent.outerWidth();
        const parentHeight = this.options.container == 'body' ? window.innerHeight : $parent.outerHeight();
        const parentLeft = this.options.container == 'body' ? 0 : $parent.offset().left;

        placement = placement == 'bottom' && pos.top + pos.height + actualHeight - docScroll > parentHeight ? 'top'
          : placement == 'top' && pos.top - docScroll - actualHeight < 0 ? 'bottom'
            : placement == 'right' && pos.right + actualWidth > parentWidth ? 'left'
              : placement == 'left' && pos.left - actualWidth < parentLeft ? 'right'
                : placement;

        $tip
          .removeClass(orgPlacement)
          .addClass(placement);
      }

      const calculatedOffset = this.getCalculatedOffset(placement, pos, actualWidth, actualHeight);

      this.applyPlacement(calculatedOffset, placement);
      this.hoverState = null;

      const complete = function () {
        that.$element.trigger(`shown.bs.${that.type}`);
      };

      $.support.transition && this.$tip.hasClass('fade')
        ? $tip
          .one($.support.transition.end, complete)
          .emulateTransitionEnd(150)
        : complete();
    }
  };

  Tooltip.prototype.applyPlacement = function (offset, placement) {
    let replace;
    const $tip = this.tip();
    const width = $tip[0].offsetWidth;
    const height = $tip[0].offsetHeight;

    // manually read margins because getBoundingClientRect includes difference
    let marginTop = parseInt($tip.css('margin-top'), 10);
    let marginLeft = parseInt($tip.css('margin-left'), 10);

    // we must check for NaN for ie 8/9
    if (isNaN(marginTop)) marginTop = 0;
    if (isNaN(marginLeft)) marginLeft = 0;

    offset.top += marginTop;
    offset.left += marginLeft;

    // $.fn.offset doesn't round pixel values
    // so we use setOffset directly with our own function B-0
    $.offset.setOffset($tip[0], $.extend({
      using(props) {
        $tip.css({
          top: Math.round(props.top),
          left: Math.round(props.left),
        });
      },
    }, offset), 0);

    $tip.addClass('in');

    // check to see if placing tip in new offset caused the tip to resize itself
    let actualWidth = $tip[0].offsetWidth;
    let actualHeight = $tip[0].offsetHeight;

    if (placement == 'top' && actualHeight != height) {
      replace = true;
      offset.top = offset.top + height - actualHeight;
    }

    if (/bottom|top/.test(placement)) {
      let delta = 0;

      if (offset.left < 0) {
        delta = offset.left * -2;
        offset.left = 0;

        $tip.offset(offset);

        actualWidth = $tip[0].offsetWidth;
        actualHeight = $tip[0].offsetHeight;
      }

      this.replaceArrow(delta - width + actualWidth, actualWidth, 'left');
    } else {
      this.replaceArrow(actualHeight - height, actualHeight, 'top');
    }

    if (replace) $tip.offset(offset);
  };

  Tooltip.prototype.replaceArrow = function (delta, dimension, position) {
    this.arrow().css(position, delta ? (`${50 * (1 - delta / dimension)}%`) : '');
  };

  Tooltip.prototype.setContent = function () {
    const $tip = this.tip();
    const title = this.getTitle();

    $tip.find('.tooltip-inner')[this.options.html ? 'html' : 'text'](title);
    $tip.removeClass('fade in top bottom left right');
  };

  Tooltip.prototype.hide = function () {
    const that = this;
    const $tip = this.tip();
    const e = $.Event(`hide.bs.${this.type}`);

    function complete() {
      if (that.hoverState != 'in') $tip.detach();
      that.$element.trigger(`hidden.bs.${that.type}`);
    }

    this.$element.trigger(e);

    if (e.isDefaultPrevented()) return;

    $tip.removeClass('in');

    $.support.transition && this.$tip.hasClass('fade')
      ? $tip
        .one($.support.transition.end, complete)
        .emulateTransitionEnd(150)
      : complete();

    this.hoverState = null;

    return this;
  };

  Tooltip.prototype.fixTitle = function () {
    const $e = this.$element;
    if ($e.attr('title') || typeof ($e.attr('data-original-title')) !== 'string') {
      $e.attr('data-original-title', $e.attr('title') || '').attr('title', '');
    }
  };

  Tooltip.prototype.hasContent = function () {
    return this.getTitle();
  };

  Tooltip.prototype.getPosition = function () {
    const el = this.$element[0];
    return $.extend({}, (typeof el.getBoundingClientRect === 'function') ? el.getBoundingClientRect() : {
      width: el.offsetWidth,
      height: el.offsetHeight,
    }, this.$element.offset());
  };

  Tooltip.prototype.getCalculatedOffset = function (placement, pos, actualWidth, actualHeight) {
    return placement == 'bottom' ? { top: pos.top + pos.height, left: pos.left + pos.width / 2 - actualWidth / 2 }
      : placement == 'top' ? { top: pos.top - actualHeight, left: pos.left + pos.width / 2 - actualWidth / 2 }
        : placement == 'left' ? { top: pos.top + pos.height / 2 - actualHeight / 2, left: pos.left - actualWidth }
        /* placement == 'right' */ : { top: pos.top + pos.height / 2 - actualHeight / 2, left: pos.left + pos.width };
  };

  Tooltip.prototype.getTitle = function () {
    let title;
    const $e = this.$element;
    const o = this.options;

    title = $e.attr('data-original-title')
      || (typeof o.title === 'function' ? o.title.call($e[0]) : o.title);

    return title;
  };

  Tooltip.prototype.tip = function () {
    return this.$tip = this.$tip || $(this.options.template);
  };

  Tooltip.prototype.arrow = function () {
    return this.$arrow = this.$arrow || this.tip().find('.tooltip-arrow');
  };

  Tooltip.prototype.validate = function () {
    if (!this.$element[0].parentNode) {
      this.hide();
      this.$element = null;
      this.options = null;
    }
  };

  Tooltip.prototype.enable = function () {
    this.enabled = true;
  };

  Tooltip.prototype.disable = function () {
    this.enabled = false;
  };

  Tooltip.prototype.toggleEnabled = function () {
    this.enabled = !this.enabled;
  };

  Tooltip.prototype.toggle = function (e) {
    const self = e ? $(e.currentTarget)[this.type](this.getDelegateOptions()).data(`bs.${this.type}`) : this;
    self.tip().hasClass('in') ? self.leave(self) : self.enter(self);
  };

  Tooltip.prototype.destroy = function () {
    clearTimeout(this.timeout);
    this.hide().$element.off(`.${this.type}`).removeData(`bs.${this.type}`);
  };

  // TOOLTIP PLUGIN DEFINITION
  // =========================

  const old = $.fn.tooltip;

  $.fn.tooltip = function (option) {
    return this.each(function () {
      const $this = $(this);
      let data = $this.data('bs.tooltip');
      const options = typeof option === 'object' && option;

      if (!data && option == 'destroy') return;
      if (!data) $this.data('bs.tooltip', (data = new Tooltip(this, options)));
      if (typeof option === 'string') data[option]();
    });
  };

  $.fn.tooltip.Constructor = Tooltip;

  // TOOLTIP NO CONFLICT
  // ===================

  $.fn.tooltip.noConflict = function () {
    $.fn.tooltip = old;
    return this;
  };
}(jQuery));

/* ========================================================================
 * Bootstrap: popover.js v3.1.1
 * http://getbootstrap.com/javascript/#popovers
 * ========================================================================
 * Copyright 2011-2014 Twitter, Inc.
 * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ======================================================================== */

+(function ($) {
  // POPOVER PUBLIC CLASS DEFINITION
  // ===============================

  const Popover = function (element, options) {
    this.init('popover', element, options);
  };

  if (!$.fn.tooltip) throw new Error('Popover requires tooltip.js');

  Popover.DEFAULTS = $.extend({}, $.fn.tooltip.Constructor.DEFAULTS, {
    placement: 'right',
    trigger: 'click',
    content: '',
    template: '<div class="popover"><div class="arrow"></div><h3 class="popover-title"></h3><div class="popover-content"></div></div>',
  });

  // NOTE: POPOVER EXTENDS tooltip.js
  // ================================

  Popover.prototype = $.extend({}, $.fn.tooltip.Constructor.prototype);

  Popover.prototype.constructor = Popover;

  Popover.prototype.getDefaults = function () {
    return Popover.DEFAULTS;
  };

  Popover.prototype.setContent = function () {
    const $tip = this.tip();
    const title = this.getTitle();
    const content = this.getContent();

    $tip.find('.popover-title')[this.options.html ? 'html' : 'text'](title);
    $tip.find('.popover-content')[// we use append for html objects to maintain js events
      this.options.html ? (typeof content === 'string' ? 'html' : 'append') : 'text'
    ](content);

    $tip.removeClass('fade top bottom left right in');

    // IE8 doesn't accept hiding via the `:empty` pseudo selector, we have to do
    // this manually by checking the contents.
    if (!$tip.find('.popover-title').html()) $tip.find('.popover-title').hide();
  };

  Popover.prototype.hasContent = function () {
    return this.getTitle() || this.getContent();
  };

  Popover.prototype.getContent = function () {
    const $e = this.$element;
    const o = this.options;

    return $e.attr('data-content')
      || (typeof o.content === 'function'
        ? o.content.call($e[0])
        : o.content);
  };

  Popover.prototype.arrow = function () {
    return this.$arrow = this.$arrow || this.tip().find('.arrow');
  };

  Popover.prototype.tip = function () {
    if (!this.$tip) this.$tip = $(this.options.template);
    return this.$tip;
  };

  // POPOVER PLUGIN DEFINITION
  // =========================

  const old = $.fn.popover;

  $.fn.popover = function (option) {
    return this.each(function () {
      const $this = $(this);
      let data = $this.data('bs.popover');
      const options = typeof option === 'object' && option;

      if (!data && option == 'destroy') return;
      if (!data) $this.data('bs.popover', (data = new Popover(this, options)));
      if (typeof option === 'string') data[option]();
    });
  };

  $.fn.popover.Constructor = Popover;

  // POPOVER NO CONFLICT
  // ===================

  $.fn.popover.noConflict = function () {
    $.fn.popover = old;
    return this;
  };
}(jQuery));

/* ========================================================================
 * Bootstrap: scrollspy.js v3.1.1
 * http://getbootstrap.com/javascript/#scrollspy
 * ========================================================================
 * Copyright 2011-2014 Twitter, Inc.
 * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ======================================================================== */

+(function ($) {
  // SCROLLSPY CLASS DEFINITION
  // ==========================

  function ScrollSpy(element, options) {
    let href;
    const process = $.proxy(this.process, this);

    this.$element = $(element).is('body') ? $(window) : $(element);
    this.$body = $('body');
    this.$scrollElement = this.$element.on('scroll.bs.scroll-spy.data-api', process);
    this.options = $.extend({}, ScrollSpy.DEFAULTS, options);
    this.selector = `${this.options.target
      || ((href = $(element).attr('href')) && href.replace(/.*(?=#[^\s]+$)/, '')) // strip for ie7
      || ''} .nav li > a`;
    this.offsets = $([]);
    this.targets = $([]);
    this.activeTarget = null;

    this.refresh();
    this.process();
  }

  ScrollSpy.DEFAULTS = {
    offset: 10,
  };

  ScrollSpy.prototype.refresh = function () {
    const offsetMethod = this.$element[0] == window ? 'offset' : 'position';

    this.offsets = $([]);
    this.targets = $([]);

    const self = this;
    const $targets = this.$body
      .find(this.selector)
      .map(function () {
        const $el = $(this);
        const href = $el.data('target') || $el.attr('href');
        const $href = /^#./.test(href) && $(href);

        return ($href
          && $href.length
          && $href.is(':visible')
          && [[$href[offsetMethod]().top + (!$.isWindow(self.$scrollElement.get(0)) && self.$scrollElement.scrollTop()), href]]) || null;
      })
      .sort((a, b) => a[0] - b[0])
      .each(function () {
        self.offsets.push(this[0]);
        self.targets.push(this[1]);
      });
  };

  ScrollSpy.prototype.process = function () {
    const scrollTop = this.$scrollElement.scrollTop() + this.options.offset;
    const scrollHeight = this.$scrollElement[0].scrollHeight || this.$body[0].scrollHeight;
    const maxScroll = scrollHeight - this.$scrollElement.height();
    const { offsets } = this;
    const { targets } = this;
    const { activeTarget } = this;
    let i;

    if (scrollTop >= maxScroll) {
      return activeTarget != (i = targets.last()[0]) && this.activate(i);
    }

    if (activeTarget && scrollTop <= offsets[0]) {
      return activeTarget != (i = targets[0]) && this.activate(i);
    }

    for (i = offsets.length; i--;) {
      activeTarget != targets[i]
        && scrollTop >= offsets[i]
        && (!offsets[i + 1] || scrollTop <= offsets[i + 1])
        && this.activate(targets[i]);
    }
  };

  ScrollSpy.prototype.activate = function (target) {
    this.activeTarget = target;

    $(this.selector)
      .parentsUntil(this.options.target, '.active')
      .removeClass('active');

    const selector = `${this.selector
    }[data-target="${target}"],${
      this.selector}[href="${target}"]`;

    let active = $(selector)
      .parents('li')
      .addClass('active');

    if (active.parent('.dropdown-menu').length) {
      active = active
        .closest('li.dropdown')
        .addClass('active');
    }

    active.trigger('activate.bs.scrollspy');
  };

  // SCROLLSPY PLUGIN DEFINITION
  // ===========================

  const old = $.fn.scrollspy;

  $.fn.scrollspy = function (option) {
    return this.each(function () {
      const $this = $(this);
      let data = $this.data('bs.scrollspy');
      const options = typeof option === 'object' && option;

      if (!data) $this.data('bs.scrollspy', (data = new ScrollSpy(this, options)));
      if (typeof option === 'string') data[option]();
    });
  };

  $.fn.scrollspy.Constructor = ScrollSpy;

  // SCROLLSPY NO CONFLICT
  // =====================

  $.fn.scrollspy.noConflict = function () {
    $.fn.scrollspy = old;
    return this;
  };

  // SCROLLSPY DATA-API
  // ==================

  $(window).on('load', () => {
    $('[data-spy="scroll"]').each(function () {
      const $spy = $(this);
      $spy.scrollspy($spy.data());
    });
  });
}(jQuery));

/* ========================================================================
 * Bootstrap: tab.js v3.1.1
 * http://getbootstrap.com/javascript/#tabs
 * ========================================================================
 * Copyright 2011-2014 Twitter, Inc.
 * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ======================================================================== */

+(function ($) {
  // TAB CLASS DEFINITION
  // ====================

  const Tab = function (element) {
    this.element = $(element);
  };

  Tab.prototype.show = function () {
    const $this = this.element;
    const $ul = $this.closest('ul:not(.dropdown-menu)');
    let selector = $this.data('target');

    if (!selector) {
      selector = $this.attr('href');
      selector = selector && selector.replace(/.*(?=#[^\s]*$)/, ''); // strip for ie7
    }

    if ($this.parent('li').hasClass('active')) return;

    const previous = $ul.find('.active:last a')[0];
    const e = $.Event('show.bs.tab', {
      relatedTarget: previous,
    });

    $this.trigger(e);

    if (e.isDefaultPrevented()) return;

    const $target = $(selector);

    this.activate($this.parent('li'), $ul);
    this.activate($target, $target.parent(), () => {
      $this.trigger({
        type: 'shown.bs.tab',
        relatedTarget: previous,
      });
    });
  };

  Tab.prototype.activate = function (element, container, callback) {
    const $active = container.find('> .active');
    const transition = callback
      && $.support.transition
      && $active.hasClass('fade');

    function next() {
      $active
        .removeClass('active')
        .find('> .dropdown-menu > .active')
        .removeClass('active');

      element.addClass('active');

      if (transition) {
        element[0].offsetWidth; // reflow for transition
        element.addClass('in');
      } else {
        element.removeClass('fade');
      }

      if (element.parent('.dropdown-menu')) {
        element.closest('li.dropdown').addClass('active');
      }

      callback && callback();
    }

    transition
      ? $active
        .one($.support.transition.end, next)
        .emulateTransitionEnd(150)
      : next();

    $active.removeClass('in');
  };

  // TAB PLUGIN DEFINITION
  // =====================

  const old = $.fn.tab;

  $.fn.tab = function (option) {
    return this.each(function () {
      const $this = $(this);
      let data = $this.data('bs.tab');

      if (!data) $this.data('bs.tab', (data = new Tab(this)));
      if (typeof option === 'string') data[option]();
    });
  };

  $.fn.tab.Constructor = Tab;

  // TAB NO CONFLICT
  // ===============

  $.fn.tab.noConflict = function () {
    $.fn.tab = old;
    return this;
  };

  // TAB DATA-API
  // ============

  $(document).on('click.bs.tab.data-api', '[data-toggle="tab"], [data-toggle="pill"]', function (e) {
    e.preventDefault();
    $(this).tab('show');
  });
}(jQuery));

/* ========================================================================
 * Bootstrap: affix.js v3.1.1
 * http://getbootstrap.com/javascript/#affix
 * ========================================================================
 * Copyright 2011-2014 Twitter, Inc.
 * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ======================================================================== */

+(function ($) {
  // AFFIX CLASS DEFINITION
  // ======================

  var Affix = function (element, options) {
    this.options = $.extend({}, Affix.DEFAULTS, options);
    this.$window = $(window)
      .on('scroll.bs.affix.data-api', $.proxy(this.checkPosition, this))
      .on('click.bs.affix.data-api', $.proxy(this.checkPositionWithEventLoop, this));

    this.$element = $(element);
    this.affixed = this.unpin = this.pinnedOffset = null;

    this.checkPosition();
  };

  Affix.RESET = 'affix affix-top affix-bottom';

  Affix.DEFAULTS = {
    offset: 0,
  };

  Affix.prototype.getPinnedOffset = function () {
    if (this.pinnedOffset) return this.pinnedOffset;
    this.$element.removeClass(Affix.RESET).addClass('affix');
    const scrollTop = this.$window.scrollTop();
    const position = this.$element.offset();
    return (this.pinnedOffset = position.top - scrollTop);
  };

  Affix.prototype.checkPositionWithEventLoop = function () {
    setTimeout($.proxy(this.checkPosition, this), 1);
  };

  Affix.prototype.checkPosition = function () {
    if (!this.$element.is(':visible')) return;

    const scrollHeight = $(document).height();
    const scrollTop = this.$window.scrollTop();
    const position = this.$element.offset();
    const { offset } = this.options;
    let offsetTop = offset.top;
    let offsetBottom = offset.bottom;

    if (this.affixed == 'top') position.top += scrollTop;

    if (typeof offset !== 'object') offsetBottom = offsetTop = offset;
    if (typeof offsetTop === 'function') offsetTop = offset.top(this.$element);
    if (typeof offsetBottom === 'function') offsetBottom = offset.bottom(this.$element);

    const affix = this.unpin != null && (scrollTop + this.unpin <= position.top) ? false
      : offsetBottom != null && (position.top + this.$element.height() >= scrollHeight - offsetBottom) ? 'bottom'
        : offsetTop != null && (scrollTop <= offsetTop) ? 'top' : false;

    if (this.affixed === affix) return;
    if (this.unpin) this.$element.css('top', '');

    const affixType = `affix${affix ? `-${affix}` : ''}`;
    const e = $.Event(`${affixType}.bs.affix`);

    this.$element.trigger(e);

    if (e.isDefaultPrevented()) return;

    this.affixed = affix;
    this.unpin = affix == 'bottom' ? this.getPinnedOffset() : null;

    this.$element
      .removeClass(Affix.RESET)
      .addClass(affixType)
      .trigger($.Event(affixType.replace('affix', 'affixed')));

    if (affix == 'bottom') {
      this.$element.offset({ top: scrollHeight - offsetBottom - this.$element.height() });
    }
  };

  // AFFIX PLUGIN DEFINITION
  // =======================

  const old = $.fn.affix;

  $.fn.affix = function (option) {
    return this.each(function () {
      const $this = $(this);
      let data = $this.data('bs.affix');
      const options = typeof option === 'object' && option;

      if (!data) $this.data('bs.affix', (data = new Affix(this, options)));
      if (typeof option === 'string') data[option]();
    });
  };

  $.fn.affix.Constructor = Affix;

  // AFFIX NO CONFLICT
  // =================

  $.fn.affix.noConflict = function () {
    $.fn.affix = old;
    return this;
  };

  // AFFIX DATA-API
  // ==============

  $(window).on('load', () => {
    $('[data-spy="affix"]').each(function () {
      const $spy = $(this);
      const data = $spy.data();

      data.offset = data.offset || {};

      if (data.offsetBottom) data.offset.bottom = data.offsetBottom;
      if (data.offsetTop) data.offset.top = data.offsetTop;

      $spy.affix(data);
    });
  });
}(jQuery));
