package com.example.projemanag.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.projemanag.activities.*
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {
    private val mFirestore = FirebaseFirestore.getInstance()

    fun getCurrentUserID (): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun registerUser(activity: SignUpActivity, user: User){
        mFirestore.collection(Constants.USERS).document(getCurrentUserID())
            .set(user, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error", e)
            }
    }

    fun loadUserData(activity: Activity, readBoardsList: Boolean = false){
        mFirestore.collection(Constants.USERS).document(getCurrentUserID()).get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)!!

                when(activity){

                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser)
                    }

                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                    }

                    is MyProfileActivity -> {
                        activity.setUserDataInUI(loggedInUser)
                    }
                }
            }
            .addOnFailureListener { e ->

                when(activity){

                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }

                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }

                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName, "Error", e)
            }
    }

    fun updateUserProfileData(activity: MyProfileActivity, useHashMap: HashMap<String,Any>){
        mFirestore.collection(Constants.USERS).document(getCurrentUserID()).update(useHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile updated successfully")
                activity.profileUpdatedSuccess()
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, e.message.toString())
                Toast.makeText(activity,"Error when updating the profile!",Toast.LENGTH_SHORT).show()
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFirestore.collection(Constants.Boards).document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "Board created successfully")
                Toast.makeText(activity,"Board created successfully", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error",e)
            }
    }

    fun getBoardsList(activity: MainActivity){
        mFirestore.collection(Constants.Boards)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID()).get()
            .addOnSuccessListener {  document ->
                Log.i(activity.javaClass.simpleName, document.documents.toString())
                val boardsList: ArrayList<Board> = ArrayList()
                for (i in document.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardsList.add(board)
                }
                activity.populateBoardsListToUi(boardsList)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName , "Error while getting boards list", e )
            }
    }

    fun getBoardDetails(activity: TaskListActivity, boardDocumentId: String){
        mFirestore.collection(Constants.Boards).document(boardDocumentId).get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName , "Error while getting tasks list", e )
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board){
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFirestore.collection(Constants.Boards).document(board.documentId).update(taskListHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "TaskList updated successfully")

                when (activity){
                    is TaskListActivity -> {
                        activity.addUpdateTaskListSuccess()
                    }

                    is CardDetailsActivity -> {
                        activity.updateCardDetailsSuccess()
                    }
                }

            }.addOnFailureListener { e ->
                when (activity){
                    is TaskListActivity -> {
                        activity.hideProgressDialog()
                    }

                    is CardDetailsActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName , "Error while updating tasks list", e )
            }
    }

    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>){
        mFirestore.collection(Constants.USERS).whereIn(Constants.ID, assignedTo).get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val usersList = ArrayList<User>()

                for (i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }

                when(activity){
                    is MembersActivity -> {
                        activity.setupMembersList(usersList)
                    }

                    is TaskListActivity -> {
                        activity.boardMembersDetailsList(usersList)
                    }
                }

            }.addOnFailureListener { e ->
                when(activity){
                    is MembersActivity -> {
                        activity.hideProgressDialog()
                    }

                    is TaskListActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName , "Error while getting members list", e )
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String){
        mFirestore.collection(Constants.EMAIL).whereEqualTo(Constants.EMAIL, email).get()
            .addOnSuccessListener { document ->

                if (document.documents.size > 0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }
                else{
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found", true)
                }

            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName , "Error while getting member's details", e )
            }
    }

    fun assignedMemberToBoard(activity: MembersActivity, board: Board, user: User){
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFirestore.collection(Constants.Boards).document(board.documentId).update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignedSuccess(user)
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName , "Error while assigning member to board", e )
            }
    }
}