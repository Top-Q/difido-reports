(function($) {
  $(function() {
    var isMouseDown = false,
      $panelOne = $(".panel.one"),
      $panelTwo = $(".panel.two"),
      $panelContainer = $panelOne.parent(),
      getParentWidth = function() {
        return $panelContainer.width();
      },
      mouseMoveHandler = function(e) {
        if (!isMouseDown) return;

        var clientX = e.clientX || (e.touches && e.touches[0].clientX);
        if(isNaN(clientX))
            return;
            
        
        var width = (clientX / getParentWidth()) * 100;

        // don't allow a value that's smaller than zero;
        width = width < 0 ? 0 : width;

        // apply size to panel 1
        $panelOne.css({ width: width + "%" });

        // apply size to panel 2
        $panelTwo.css({ width: 100 - width + "%" });
      };

    // mouseDown event
    $(".slider").on("mousedown touchstart", function() {
      // only bind a the mouseMove handler on the first cycle
      !isMouseDown && $panelContainer.on("mousemove touchmove", mouseMoveHandler);
      isMouseDown = true;
    });

    $(window).on("mouseup touchend", function() {
      isMouseDown = false;
      // detach then mouseMove handler
      $panelContainer.off("mousemove touchmove");
    });
  });
})(jQuery);
