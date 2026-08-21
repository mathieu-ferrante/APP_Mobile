package com.phoenix.fitpro.data.repository

import android.content.Context
import com.phoenix.fitpro.domain.ai.AiProgramGenerator
import com.phoenix.fitpro.domain.model.CoachUserProfile
import com.phoenix.fitpro.domain.model.TrainingProgram
import com.phoenix.fitpro.domain.repository.ProgramRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgramRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : ProgramRepository {

    private val prefs = context.getSharedPreferences("ashes_coach_program_prefs", Context.MODE_PRIVATE)

    private val _programFlow = MutableStateFlow<TrainingProgram?>(null)
    private val _profileFlow = MutableStateFlow(CoachUserProfile())

    init {
        loadData()
    }

    private fun loadData() {
        val profileJson = prefs.getString(KEY_COACH_PROFILE, null)
        val loadedProfile = if (profileJson != null) {
            try {
                gson.fromJson(profileJson, CoachUserProfile::class.java)
            } catch (e: Exception) {
                CoachUserProfile()
            }
        } else {
            CoachUserProfile()
        }
        _profileFlow.value = loadedProfile

        val programJson = prefs.getString(KEY_TRAINING_PROGRAM, null)
        val loadedProgram = if (programJson != null) {
            try {
                gson.fromJson(programJson, TrainingProgram::class.java)
            } catch (e: Exception) {
                AiProgramGenerator.generateTailoredProgram(loadedProfile)
            }
        } else {
            // Generate default custom program on first run
            val defaultProg = AiProgramGenerator.generateTailoredProgram(loadedProfile)
            saveProgramSync(defaultProg)
            defaultProg
        }
        _programFlow.value = loadedProgram
    }

    override fun observeCurrentProgram(): Flow<TrainingProgram?> = _programFlow.asStateFlow()

    override suspend fun getCurrentProgram(): TrainingProgram? = _programFlow.value

    override suspend fun saveProgram(program: TrainingProgram) {
        saveProgramSync(program)
        _programFlow.value = program
    }

    private fun saveProgramSync(program: TrainingProgram) {
        val json = gson.toJson(program)
        prefs.edit().putString(KEY_TRAINING_PROGRAM, json).apply()
    }

    override fun observeCoachProfile(): Flow<CoachUserProfile> = _profileFlow.asStateFlow()

    override suspend fun getCoachProfile(): CoachUserProfile = _profileFlow.value

    override suspend fun saveCoachProfile(profile: CoachUserProfile) {
        val json = gson.toJson(profile)
        prefs.edit().putString(KEY_COACH_PROFILE, json).apply()
        _profileFlow.value = profile
    }

    companion object {
        private const val KEY_TRAINING_PROGRAM = "key_training_program"
        private const val KEY_COACH_PROFILE = "key_coach_profile"
    }
}
