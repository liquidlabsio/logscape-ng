/* globals Chart:false, feather:false */

// (function () {
$(document).ready(function () {
  'use strict'

  feather.replace()

  new Logscape.Navigation();

  $(".nav-link.active").trigger("click")

  $("#signout").click(function() {
    window.localStorage.removeItem("logscape-auth")
    window.location.href = "signin.html"
  })

});

Logscape.Navigation = function () {
  console.log("Logscape.Navigation created")
  setupNavigationActions();
  setupNavigationHideShow();


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
  function setupNavigationHideShow() {
        $(".compressed").hide();
        $("#compressNav").click(function(event){
            $(".expanded").hide();
            $(".compressed").show();

            $("#sideNav").addClass("navCompressed");

            // configure lhs padding of main panel
            $("#mainPanel").removeClass("ml-sm-auto");
            $("#mainPanel").addClass("ml-4");

            // make main panel expand to full width
            $("#mainPanel").removeClass("col-lg-10");
            $("#mainPanel").removeClass("col-md-10");
            $("#mainPanel").addClass("col-lg-11");
            $("#mainPanel").addClass("col-md-11");

        })
        $("#expandNav").click(function(event){
            $(".compressed").hide();
            $(".expanded").show();
            $("#sideNav").removeClass("navCompressed");

            // configure lhs padding of main panel
            $("#mainPanel").removeClass("ml-4");
            $("#mainPanel").addClass("ml-sm-auto");

            // make main panel expand to full width
            $("#mainPanel").removeClass("col-lg-11");
            $("#mainPanel").removeClass("col-md-11");
            $("#mainPanel").addClass("col-lg-10");
            $("#mainPanel").addClass("col-md-10");
        })
  }
}



