'use strict';

/* Controllers */

var phonecatControllers = angular.module('phonecatControllers', []);

phonecatControllers.controller('PhoneListCtrl', ['$scope', '$http', '$interval',
  function($scope, $http, $interval) {
  	$scope.refreshInterval = 30; // For every 10 sec
  	function refresh() {
  		console.log("refreshing");
	    $http.get('http://ec2-54-67-43-16.us-west-1.compute.amazonaws.com:8083/topics?req=topics1h').success(function(data) {
	      $scope.topics1h = data;
	    });
	    $http.get('http://ec2-54-67-43-16.us-west-1.compute.amazonaws.com:8083/topics?req=topics12h').success(function(data) {
	      $scope.topics12h = data;
	    });
	    $http.get('http://ec2-54-67-43-16.us-west-1.compute.amazonaws.com:8083/topics?req=topicsalltime').success(function(data) {
	      $scope.topicsalltime = data;
	    });
	}
	refresh(); 
	/*$interval(function() { 
	refresh();
	}, $scope.refreshInterval * 1000);*/
  }]);


phonecatControllers.controller('PhoneDetailCtrl', ['$scope', '$routeParams', '$http', '$interval',
  function($scope, $routeParams, $http, $interval) {

  	//GETTING THE TRENDS
  	function refreshTopics() {
  		console.log("refreshing");
	    $http.get('http://ec2-54-67-43-16.us-west-1.compute.amazonaws.com:8083/topics?req=topics1h').success(function(data) {
	      $scope.topics1h = data;
	    });
	    $http.get('http://ec2-54-67-43-16.us-west-1.compute.amazonaws.com:8083/topics?req=topics12h').success(function(data) {
	      $scope.topics12h = data;
	    });
	    $http.get('http://ec2-54-67-43-16.us-west-1.compute.amazonaws.com:8083/topics?req=topicsalltime').success(function(data) {
	      $scope.topicsalltime = data;
	    });
	}

	$scope.limitTopics = 10;
	$scope.incrementLimit = function() {
    	$scope.limitTopics += 10;
	};
	

	$scope.map = { center: { latitude: 45, longitude: 0}, zoom: 1 };

  	$scope.refreshInterval = 5; // For every 5 sec
  	function refreshComments() {
  		//Flat comments organization
  		if(!$scope.nest){
		    $http.get('http://ec2-54-67-43-16.us-west-1.compute.amazonaws.com:8083/comments?org=flat&req=' + $routeParams.topic).success(function(data) {
		      $scope.comments = data;
		      $scope.loadedComments=true;
		    });
		}
		//Nested comments organization
		else{
		    $http.get('http://ec2-54-67-43-16.us-west-1.compute.amazonaws.com:8083/comments?org=nest&req=' + $routeParams.topic).success(function(data) {
		      $scope.articles = data;
		      $scope.loadedComments=true;
		      $scope.nested_article = Array.apply(null, new Array($scope.articles.length)).map(Boolean.prototype.valueOf,true);
		    });
		}

	}

	function addMarkers(tweets){
    	angular.forEach(tweets, function(tweet){
	  		if(tweet.longitude !== 0.0){
	      		this.push({
		      		latitude: tweet.latitude,
		      		longitude: tweet.longitude,
		      		title:tweet.message,
		      		id:tweet.id
		      	});
	      	}
		}, $scope.tweetMarkers);
	}

	function initTweets(){
		//Getting the tweets
		$http.get('http://ec2-54-67-43-16.us-west-1.compute.amazonaws.com:8083/tweets?req=' + $routeParams.topic).success(function(data) {
		      $scope.tweets = data.tweets;
		      $scope.timestampTweets = data.timestamp;
		      $scope.loadedTweets = true;
		      //Adding the markers to the map
		      addMarkers($scope.tweets)
		      //Getting new tweets every 5 seconds
	      	  $interval(function() { 
				refreshTweets();
			  }, $scope.refreshInterval * 1000);
	    });
	}


	function refreshTweets(){
		//Getting the tweets
		$http.get('http://ec2-54-67-43-16.us-west-1.compute.amazonaws.com:8083/tweets?timestampBack='+$scope.timestampTweets+'&req=' + $routeParams.topic).success(function(data) {
			if(!jQuery.isEmptyObject(data.tweets)){
			    $scope.tweets = data.tweets.concat($scope.tweets);
			    $scope.timestampTweets = data.timestamp;
			    //Adding the markers to the map
		      	addMarkers($scope.tweets)
			}
	    });
	}

	$scope.loadedComments = false;
	$scope.loadedTweets = false;
	$scope.nest= true;
	$scope.tweetMarkers = [];

	$scope.$watch('nest', function() {
		$scope.loadedComments = false;
    	refreshComments();
   	});

   	$scope.toggle = function(article){
   		console.log(article)
   		$scope.nested_article[article] = !$scope.nested_article[article];
   	}



    $scope.sortByDate = function(comment) {
			return Date.parse(comment.created_time);
	};

	$scope.orderProp = '-like_count';
	$scope.topic = $routeParams.topic;

	refreshTopics();
	/*Getting and refreshing topics every minutes*/
	$interval(function() { 
		refreshTopics();
	}, 60 * 1000);

	initTweets();
  }]);