package com.test.dontforget.Repository

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.test.dontforget.DAO.TodoClass

class TodoRepository {

    companion object {

        // 할일 인덱스 가져오기
        fun getTodoIdx(callback1: (Task<DataSnapshot>) -> Unit) {
            val database = FirebaseDatabase.getInstance()
            // 게시글 인덱스 번호
            val todoIdxRef = database.getReference("TodoIdx")
            todoIdxRef.get().addOnCompleteListener(callback1)
        }

        // 할일 삭제
        fun removeTodo(todoIdx: Long, callback1: (Task<Void>) -> Unit) {
            val database = FirebaseDatabase.getInstance()
            val testDataRef = database.getReference("todoInfo")

            testDataRef.orderByChild("todoIdx").equalTo(todoIdx.toDouble()).get()
                .addOnCompleteListener {
                    for (a1 in it.result.children) {
                        // 해당 데이터 삭제
                        a1.ref.removeValue().addOnCompleteListener(callback1)
                    }
                }
        }

        // 할일 수정
        fun modifyTodo(todoDataClass: TodoClass, callback1: (Task<Void>) -> Unit) {
            val database = FirebaseDatabase.getInstance()
            val todoDataRef = database.getReference("todoInfo")

            todoDataRef.orderByChild("todoIdx").equalTo(todoDataClass.todoIdx.toDouble()).get()
                .addOnCompleteListener {
                    for (a1 in it.result.children) {
                        a1.ref.child("todoContent").setValue(todoDataClass.todoContent)
                        a1.ref.child("todoDate").setValue(todoDataClass.todoDate)
                        a1.ref.child("todoAlertTime").setValue(todoDataClass.todoAlertTime)
                        a1.ref.child("todoLocationName").setValue(todoDataClass.todoLocationName)
                        a1.ref.child("todoLocationLatitude")
                            .setValue(todoDataClass.todoLocationLatitude)
                        a1.ref.child("todoIsChecked").setValue(todoDataClass.todoIsChecked)
                        a1.ref.child("todoLocationLongitude")
                            .setValue(todoDataClass.todoLocationLongitude)
                        a1.ref.child("todoMemo")
                            .setValue(todoDataClass.todoMemo)
                            .addOnCompleteListener(callback1)
                        a1.ref.child("todoMemo").setValue(todoDataClass.todoMemo)
                        a1.ref.child("todoOwnerName")
                            .setValue(todoDataClass.todoOwnerName)
                            .addOnCompleteListener(callback1)
                    }
                }
        }

        // 해당 인덱스 할일 정보 가져오기
        fun getTodoInfoByIdx(todoIdx: Long, callback1: (Task<DataSnapshot>) -> Unit) {
            val database = FirebaseDatabase.getInstance()
            val todoDataRef = database.getReference("todoInfo")
            todoDataRef.orderByChild("todoIdx").equalTo(todoIdx.toDouble()).get()
                .addOnCompleteListener(callback1)
        }


        // 날짜로 할일 정보 가져오기
        fun getTodoInfoByDate(todoDate: String, callback1: (Task<DataSnapshot>) -> Unit) {
            val database = FirebaseDatabase.getInstance()
            val todoDataRef = database.getReference("todoInfo")
            todoDataRef.orderByChild("todoDate").equalTo(todoDate).get()
                .addOnCompleteListener(callback1)
        }

        //할일 추가 인덱스 저장
        fun setTodoIdx(todoIdx: Long, callback1: (Task<Void>) -> Unit) {
            val database = FirebaseDatabase.getInstance()
            val databaseRef = database.getReference("TodoIdx")
            databaseRef.get().addOnCompleteListener {
                it.result.ref.setValue(todoIdx).addOnCompleteListener(callback1)
            }
        }

        //할일 추가 정보 저장
        fun setTodoAddInfo(todoClass: TodoClass, callback1: (Task<Void>) -> Unit) {
            val database = FirebaseDatabase.getInstance()
            val databaseRef = database.getReference("todoInfo")
            databaseRef.push().setValue(todoClass).addOnCompleteListener(callback1)

        }

        // 날짜순으로 할일 정보 가져오기
        fun getAllTodo(callback1: (Task<DataSnapshot>) -> Unit) {
            val database = FirebaseDatabase.getInstance()
            val databaseRef = database.getReference("todoInfo")
            databaseRef.orderByChild("todoDate").get().addOnCompleteListener(callback1)
        }

        // 해당 카테고리의 할일 정보 가져오기
        fun getTodoInfoByCategory(todoCategoryIdx: Long, callback1: (Task<DataSnapshot>) -> Unit) {
            val database = FirebaseDatabase.getInstance()
            val todoDataRef = database.getReference("todoInfo")
            todoDataRef.orderByChild("todoCategoryIdx").equalTo(todoCategoryIdx.toDouble()).get()
                .addOnCompleteListener(callback1)
        }

        // 해당 유저의 할일 삭제
        fun removeTodoByUserIdx(userIdx: Long, callback1: (Task<Void>) -> Unit) {
            val database = FirebaseDatabase.getInstance()
            val testDataRef = database.getReference("todoInfo")

            testDataRef.orderByChild("todoOwnerIdx").equalTo(userIdx.toDouble()).get()
                .addOnCompleteListener {
                    for (a1 in it.result.children) {
                        // 해당 데이터 삭제
                        a1.ref.removeValue().addOnCompleteListener(callback1)
                    }
                }
        }

        // 해당 카테고리의 할일 정보 수정
        fun modifyTodoByCategory(todoDataClass: TodoClass, callback1: (Task<Void>) -> Unit) {
            val database = FirebaseDatabase.getInstance()
            val todoDataRef = database.getReference("todoInfo")

            todoDataRef.orderByChild("todoIdx").equalTo(todoDataClass.todoIdx.toDouble()).get()
                .addOnCompleteListener {
                    for (a1 in it.result.children) {
                        a1.ref.child("todoCategoryName").setValue(todoDataClass.todoCategoryName)
                        a1.ref.child("todoBackgroundColor").setValue(todoDataClass.todoBackgroundColor)
                        a1.ref.child("todoFontColor").setValue(todoDataClass.todoFontColor).addOnCompleteListener(callback1)
                        a1.ref.child("todoOwnerName").setValue(todoDataClass.todoOwnerName)
                    }
                }
        }

    }
}