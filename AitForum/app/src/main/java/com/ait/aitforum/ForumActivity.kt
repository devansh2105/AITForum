package com.ait.aitforum

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ait.aitforum.adapter.PostsAdapter
import com.ait.aitforum.data.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_forum.*

class ForumActivity : AppCompatActivity() {

    private lateinit var postsAdapter: PostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forum)
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            startActivity(
                Intent(this@ForumActivity, CreatePostActivity::class.java)
            )
        }

        postsAdapter = PostsAdapter(this, FirebaseAuth.getInstance().currentUser!!.uid)

        var linLayoutManager = LinearLayoutManager(this)
        linLayoutManager.reverseLayout = true
        linLayoutManager.stackFromEnd = true

        recyclerPosts.layoutManager = linLayoutManager

        recyclerPosts.adapter = postsAdapter

        queryPosts()
    }

    fun queryPosts() {
        val db = FirebaseFirestore.getInstance()
        val query = db.collection("posts")

        var allPostsListener = query.addSnapshotListener(
            object: EventListener<QuerySnapshot> {
                override fun onEvent(querySnapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {

                    if (e != null) {
                        Toast.makeText(this@ForumActivity, "listen error: ${e.message}", Toast.LENGTH_LONG).show()
                        return
                    }

                    for (dc in querySnapshot!!.getDocumentChanges()) {
                        when (dc.getType()) {
                            DocumentChange.Type.ADDED -> {
                                val post = dc.document.toObject(Post::class.java)
                                postsAdapter.addPost(post, dc.document.id)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                Toast.makeText(this@ForumActivity, "update: ${dc.document.id}", Toast.LENGTH_LONG).show()
                            }
                            DocumentChange.Type.REMOVED -> {
                                postsAdapter.removePostByKey(dc.document.id)
                            }
                        }
                    }
                }
            })
    }
}
