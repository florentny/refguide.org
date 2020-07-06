'use strict';

/* Controllers */

//angular.module('myApp', [], function($locationProvider) {
//      $locationProvider.html5Mode(true).hashPrefix('!');;
//    });

function ListController($scope, $http) {
    $scope.querycharacters = 0;
    //$scope.fullname = "none";
   // $scope.sciname = "none";
   
    $scope.regions = [
        {id: 0, name: 'All', path: "", code: "all"},
        {id: 1, name: 'Caribbean', path: "carib/", code: "caribbean"},
        {id: 2, name: 'Indo-Pacific', path: "indopac/", code: "indopac"},
        {id: 3, name: 'Florida Keys', path: "keys/", code: "florida"},
        {id: 4, name: 'Hawaii', path: "hawaii/", code: "hawaii"},
        {id: 5, name: 'Baja California', path: "baja/", code: "baja"}
    ];
    
    var region = GetUrlValue('area');//$location.search()['area'];
    if(!isInteger(region))
        region = 0;
    region = parseInt(region);
    if((region < 0) || (region > 5))
        region = 0;
    
    $scope.selRegion = $scope.regions[region]; 
    
    $scope.busyIndicator = false;
    $http.get('php/reefspeciescat.php?region='+$scope.selRegion.id).success(function(data) {  
        $scope.speciesList = data;
        $scope.busyIndicator = false;
    });
    
    $scope.regionChanged = function() {
        $scope.busyIndicator = true;
        $http.get('php/reefspeciescat.php?region='+$scope.selRegion.id).success(function(data) {
            $scope.speciesList = data;
            $scope.busyIndicator = false;
        });
    };
    
    $scope.checkfewmatch = function(count) {
        if((count > 0) && (count < 5))
           return true;
        return false;
    };
    
    $scope.queryChanged = function() {
        if($scope.query.length < 3)
            $scope.querycharacters = 0;
        else
            $scope.querycharacters = 2000;
    };
    
    
    $scope.doShow = function(species) {
        return "pix/thumb/" + species.name + species.thumb1 + ".jpg";
    };
    
    
    $scope.ReefSearch = function(species) {
        var words = $scope.query.split(" ");
        var hit = true;
        var hit1 = true;
        var hit2 = true;
        var hit3 = true;
        for (var i = 0; i < words.length; i++) {
            hit = hit && (species.fullname.toLowerCase().indexOf(words[i].toLowerCase()) >= 0);
            hit1 = hit1 && (species.sname.toLowerCase().indexOf(words[i].toLowerCase()) >= 0);
            hit2 = hit2 && (species.subcategory.toLowerCase().indexOf(words[i].toLowerCase()) >= 0);
            hit3 = hit3 && (species.category.toLowerCase().indexOf(words[i].toLowerCase()) >= 0);
        }
        $scope.Xquery = $scope.query.replace(/ /g, "+");
        return hit || hit1 || hit2 || hit3;
    };
    
    function isInteger(value) {
        if ((undefined === value) || (null === value)) {
            return false;
        }
        return value % 1 == 0;
    };
    
    function GetUrlValue(VarSearch) {
        var SearchString = window.location.search.substring(1);
        var VariableArray = SearchString.split('&');
        for (var i = 0; i < VariableArray.length; i++) {
            var KeyValuePair = VariableArray[i].split('=');
            if (KeyValuePair[0] == VarSearch) {
                return KeyValuePair[1];
            }
        }
    };

}
    