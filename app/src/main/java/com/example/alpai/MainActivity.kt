package com.example.alpai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.alpai.routing.AppRouting
import com.example.alpai.viewmodel.ClassifierHelper

class MainActivity : ComponentActivity() {
    private lateinit var classifier: ClassifierHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classifier = ClassifierHelper(this)

        setContent {
            AppRouting(classifier)
        }
    }

    override fun onDestroy() {
        classifier.close()
        super.onDestroy()
    }
}