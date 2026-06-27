package com.stepandemianenko.dsaprep

import android.app.Application
import com.stepandemianenko.dsaprep.data.StudyDatabase
import com.stepandemianenko.dsaprep.data.StudyRepository

class StudyPlanApplication : Application() {
    val repository: StudyRepository by lazy {
        StudyRepository(StudyDatabase.getDatabase(this).studyDao())
    }
}
