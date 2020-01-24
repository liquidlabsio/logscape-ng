/* globals Chart:false, feather:false */

// (function () {
$(document).ready(function () {
  'use strict'

  feather.replace()

  new Logscape.Navigation();

  $(".nav-link.active").trigger("click")

});

Logscape.Navigation = function () {
  console.log("Logscape.Navigation created")
  setupNavigationActions();


    $("#tenantInfo").val(DEFAULT_TENANT)
    $("#restAPIInfo").val(LOGSCAPE_URL)

  function setupNavigationActions() {

    $(".nav-link").click(function (event) {
      $(".nav-link").removeClass("active")
      $(event.currentTarget).addClass("active")

      $(".mainPanelTab").hide()
      $("#"+event.currentTarget.dataset.target).show()
    })


  }
}



