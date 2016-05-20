<?php
// http://www.tutorialspoint.com/mongodb/mongodb_php.htm
 // connect to mongodb
   $m = new MongoClient();
	
   echo "Connection to database successfully";
   // select a database
   $db = $m->test;
	
   echo "Database mydb selected";

   $cursor = $db->gt->find();
   foreach ($cursor as $collection){
       echo "<br />New record: ";
       print_r($collection);
   }
   
   /*$result = array();
   $result["user"] = "Gasper";
   $result["picke"] = array("Neza", "Te,", "Tja");
   
   $db->gt->insert($result);
   
   echo "<br /><br />";
   echo json_encode($result);*/
   echo "Count {'user': 'Gasper'}: ". $db->gt->count(array("user" => "Gasper"));
   echo "Push me";
   $db->gt->update(array("user" => "Gasper12"), array('$push' => array('picke' => array('$each' => array("Kmet", "bardba", "ba")))));