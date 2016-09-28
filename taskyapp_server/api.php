<!--
  ~ Copyright (c) 2016, University of Ljubljana, Slovenia
  ~
  ~ Gasper Urh, gu7668@student.uni-lj.si
  ~
  ~ This project was developed as part of the paper submitted for the UbitTention workshop (in conjunction with UbiComp'16) and my master thesis. For more information, please visit http://projects.hcilab.org/ubittention/
  ~
  ~ Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
  ~ THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  -->
<?php 
// This script is the main api script to handle all calls and connects to the MongoDB

$action = filter_input(INPUT_GET, "action");

$usersCollection = (new MongoClient())->taskyapp->users;
// Changed from taskyapp->users_nonlabeled to taskyapp->users_nonlabeled1 
// on 28.4.2016 17:50, due to collection size limit 16MB, nonlabeled2 to 3 on 4.5.2016 23.20
$usersNonLabeledCollection = (new MongoClient())->taskyapp->users_nonlabeled3;

switch ($action) {
    case "post_records":
        postRecords();
        break;
    case "opt_out":
        deleteUserData();
        break;
    case "leaderboard_message":
        generateLeaderboardMessage();
        //showWinnerMessage();
        break;
    case "info":
        echo "<h3>This is TaskyApp's server, say hi!</h3>";
        break;
    default:
        echo "Action not handled!!! ($action)";
        break;
}

function deleteUserData(){
    global $usersCollection;
    
    $resultArray = array();
    $resultArray["success"] = false;
    $inputJSON = file_get_contents('php://input');
    $input= json_decode( $inputJSON, TRUE ); //convert JSON into array
    if(isset($input) && $input != NULL){
        $authId = (string)$input["auth"]["device_id"];        
        $where = array("auth.device_id" => $authId);
        
        $result = $usersCollection->remove($where);
        
        print_r($result);
        if($result["ok"]){
            $resultArray["success"] = true;
            $resultArray["removed"] = $result["n"];
        }
    }
    
    echo json_encode($resultArray);
}

function generateLeaderboardMessage(){
    global $usersCollection;
    
    $inputJSON = file_get_contents('php://input');
    $input= json_decode( $inputJSON, TRUE );
    $myDeviceId = (string)$input["auth"]["device_id"];   
    
    $resultArray = array();
    
    $agrArray =  [ [ '$project' => [ 'auth' => 1, 'num_readings' => [ '$size' => '$data' ] ] ] ];
    $resultAggr = $usersCollection->aggregate($agrArray);
    $usrNumRecordsArray = array();
    $myNumRecords;
    foreach($resultAggr["result"] as $usr){
        $deviceId = $usr["auth"]["device_id"];
        $usrNumRecordsArray[$usr["auth"]["device_id"]] = $usr["num_readings"];
        
    }
    arsort($usrNumRecordsArray, 1);
    $ind = 0;
    foreach ($usrNumRecordsArray as $key => $value) {
        if($key === $myDeviceId){
            break;
        }
        $ind++;
    }
    $percentage = $ind / (count($usrNumRecordsArray)-1);
    if($percentage <= 0.2){
        $leaderboardMessage = "Well done! You are among 20% of all TaskyApp users.\n\n"
                . "Your chances of winning a voucher are very high, keep up the good work.";
    }
    else if($percentage <= 0.5){
        $leaderboardMessage = "You are falling behind 20% of our users, but you are still among 50%.\n\n"
                . "Good news! You are still in the race for a 50€ voucher.";
    }
    else{
        $leaderboardMessage = "You are in the bottom half of all TaskyApp users.\n\n"
                . "You will need to label tasks more frequently if you want to win a 50€ voucher.";
    }
    
    $resultArray["success"] = true;
    $resultArray["title"] = "Leaderboard";
    $resultArray["message"] = $leaderboardMessage;
    $resultArray["hide_message"] = false;
    echo json_encode($resultArray);
}

function showWinnerMessage(){
    $winnerDeviceId = "b5a838db2b5ce0db";
    
    $inputJSON = file_get_contents('php://input');
    $input= json_decode( $inputJSON, TRUE ); //convert JSON into array
    $myDeviceId = (string)$input["auth"]["device_id"];
    
    if(strcmp($myDeviceId, $winnerDeviceId) == 0){
        
        $firstFour = strtoupper(substr($myDeviceId, 0, 4));
        $title = "CONGRATULATIONS!";
        $message = "You are the lucky winner of the 50€ voucher! We thank you for your contribution. <br /><br />"
                . "Please send us the following sequence for verification:<br /> "
                . "<b>$firstFour</b><br /> "
                . "with your contact details to <br />"
                . "<b>gu7668@student.uni-lj.si</b>";
    }
    else{
        $title = "Thank you for helping us!";
        $message = "The study has finished. We are very thankful for your support "
                . "and contribution in our study. <br /> <br />"
                . "Unfortunately you are not the lucky winner of the 50€ voucher.";
    }
    
    $resultArray["success"] = true;
    $resultArray["title"] = $title;
    $resultArray["message"] = $message;
    $resultArray["hide_message"] = false;
    echo json_encode($resultArray);
}
function postRecords(){
    global $usersCollection, $usersNonLabeledCollection;
    $resultArray = array();
    $resultArray["success"] = false;
    $inputJSON = file_get_contents('php://input');
    $input= json_decode( $inputJSON, TRUE ); //convert JSON into array
    
    if(isset($input) && $input != NULL){
        $authId = (string)$input["auth"]["device_id"];
        
        $dataList = $input["data"];

        $where = array("auth.device_id" => $authId);
        
        $confirmIds = array();
        $alreadyExistedIds = array();
        foreach ($dataList as $data) {
            
            if(array_key_exists("label", $data) && $data["label"] > 0){
                $dbCollectionToSave = $usersCollection;
            }
            else{
                $dbCollectionToSave = $usersNonLabeledCollection;
            }
            
            $whereCheckExists = array("auth.device_id" => $authId, "data.database_id" => $data["database_id"]);
            $res = $dbCollectionToSave->find($whereCheckExists);
           
            if($res->count() == 0){
                // "There are no records with that database id yet, insert";
                try{
                    $result = $dbCollectionToSave->update($where, 
                        array('$push' => array('data' => $data)), 
                        array("upsert" => true));
                    
                    if($result["ok"] == 1){
                        $confirmIds[] = $data["database_id"];
                    }
                } catch(Exception $e){
                    error_log("Using collection: " . $dbCollectionToSave->getName());
                    error_log($e->getTraceAsString());
                }
            }
            else{
                // There is a record with that id, just confirm it, so client will delete it.
                $confirmIds[] = $data["database_id"];
                $alreadyExistedIds[] = $data["database_id"];
            }

            
        };
        
        // Update auth field
        $updateValue = array('$set' => array("auth" => $input["auth"]));
        $result = $usersNonLabeledCollection->update($where, $updateValue);
        $result = $usersCollection->update($where, $updateValue);
       

        //}
        //echo "Modified: " . $result->getModifiedCount() . ", Matched: " . $result->getMatchedCount() . " Inserted: " . $result->getInsertedCount();

        
        if(count($confirmIds) > 0){
            $resultArray["success"] = true;
            $resultArray["confirm_ids"] = $confirmIds;
            $resultArray["alredy_existed_ids"] = $alreadyExistedIds;
        }
        
      

    } else{
        $resultArray["err_msg"] = "It appears that input JSON is not set correctly!";
    }
    echo json_encode($resultArray);
}
