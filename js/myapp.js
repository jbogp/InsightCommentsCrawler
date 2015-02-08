'use strict';

/* App Module */

var phonecatApp = angular.module('phonecatApp', [
  'ngRoute',
  'phonecatControllers',
  'uiGmapgoogle-maps',
  'hc.marked',
  'angulartics',
  'angulartics.google.analytics'
]);

phonecatApp.config(['markedProvider', function(markedProvider) {
  markedProvider.setOptions({gfm: true});
}]);

phonecatApp.config(function ($analyticsProvider) {
            $analyticsProvider.firstPageview(true); /* Records pages that don't use $state or $route */
            $analyticsProvider.withAutoBase(true);  /* Records full path */
    });

phonecatApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/topics/', {
        templateUrl: 'partials/topic-list.html',
        controller: 'PhoneListCtrl'
      }).
      when('/comments/:topic', {
        templateUrl: 'partials/comments-list.html',
        controller: 'PhoneDetailCtrl'
      }).
      when('/blog/', {
        templateUrl: 'partials/blog.html',
        controller: 'BlogCtrl'
      }).
      when('/demo/', {
        templateUrl: 'partials/demo.html',
        controller: 'BlogCtrl'
      }).
      when('/contact/', {
        templateUrl: 'partials/contact.html',
        controller: 'BlogCtrl'
      }).
      when('/stats/', {
        templateUrl: 'partials/stats.html',
        controller: 'StatsCtrl'
      }).
      otherwise({
        redirectTo: '/topics/'
      });
  }]);
