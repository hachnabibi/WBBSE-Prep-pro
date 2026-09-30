package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.QuestionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.DeshExamViewModel
import com.example.ui.components.BannerAd
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class LiveMockQuestionItem(
    val id: String,
    val questionBengali: String,
    val questionEnglish: String,
    val optionsBengali: List<String>,
    val optionsEnglish: List<String>,
    val correctIndex: Int,
    val explanationBengali: String,
    val explanationEnglish: String
)

data class LiveMockArena(
    val id: String,
    val titleBengali: String,
    val titleEnglish: String,
    val subtitleBengali: String,
    val subtitleEnglish: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val preNumber: Int,
    val durationMinutes: Int = 15,
    val questionCount: Int = 15,
    val questions: List<LiveMockQuestionItem>
)

data class LiveMockLeaderboardUser(
    val name: String,
    val district: String = "",
    val score: Int,
    val maxScore: Int = 60,
    val avatarColor: Color,
    val isUser: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMockTestScreen(
    viewModel: DeshExamViewModel? = null,
    isBengali: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val dbQuestions = viewModel?.questions?.collectAsStateWithLifecycle()?.value ?: emptyList()
    val studentProfile = viewModel?.studentProfile?.collectAsStateWithLifecycle()?.value

    // Distinct Subject Arenas for Live Mock Tests (All 60 Questions, 60 Minutes)
    val mockArenas = remember(dbQuestions) {
        listOf(
            LiveMockArena(
                id = "bengali",
                titleBengali = "বাংলা লাইভ মক টেস্ট",
                titleEnglish = "Bengali Live Mock Test",
                subtitleBengali = "সাহিত্য, কবিতা, ব্যাকরণ ও পাঠ্যবই",
                subtitleEnglish = "Literature, Grammar & Text",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                primaryColor = Color(0xFF8B5CF6),
                preNumber = 650,
                durationMinutes = 60,
                questionCount = 60,
                questions = listOf(
                    LiveMockQuestionItem("ben_m1", "‘অদল বদল’ গল্পের লেখক কে?", "Who is the author of 'Adal Badal'?", listOf("পান্নালাল প্যাটেল", "শরৎচন্দ্র চট্টোপাধ্যায়", "তারাশঙ্কর বন্দ্যোপাধ্যায়", "বিভূতিভূষণ বন্দ্যোপাধ্যায়"), listOf("Pannalal Patel", "Sarat Chandra", "Tarashankar", "Bibhutibhushan"), 0, "‘অদল বদল’ গল্পের লেখক গুজরাতি সাহিত্যিক পান্নালাল প্যাটেল।", "Author is Pannalal Patel."),
                    LiveMockQuestionItem("ben_m2", "হরিদার জীবনের সবচেয়ে বড় পেশা বা শখ কী ছিল?", "What was Harida's hobby/passion?", listOf("বহুরূপী সাজা", "দোকানদারি", "শিক্ষকতা", "কৃষিকাজ"), listOf("Bahurupi disguise", "Shopkeeping", "Teaching", "Farming"), 0, "হরিদা বিচিত্র সব বহুরূপী সেজে জীবিকা নির্বাহ করতেন।", "Harida was a Bahurupi."),
                    LiveMockQuestionItem("ben_m3", "‘জ্ঞানচক্ষু’ গল্পে তপনের লেখা গল্পটির নাম কী ছিল?", "What was the name of Tapan's story?", listOf("প্রথম দিন", "স্কুলের দিন", "ছুটির দিন", "আমার জীবন"), listOf("Prothom Din", "Schooler Din", "Chhutir Din", "Amar Jibon"), 0, "তপনের লেখা প্রথম গল্পটির নাম ছিল ‘প্রথম দিন’।", "Story name was 'Prothom Din'."),
                    LiveMockQuestionItem("ben_m4", "‘পথের দাবী’ রচনায় নিমাইবাবু কোন পদে ছিলেন?", "What was Nimai Babu's post in 'Pather Dabi'?", listOf("পুলিশ সুপারিন্টেন্ডেন্ট", "দারোগা", "ম্যাজিস্ট্রেট", "আইসি"), listOf("Police Superintendent", "Daroga", "Magistrate", "IC"), 0, "নিমাইবাবু ছিলেন পুলিশ সুপারিন্টেন্ডেন্ট।", "Nimai Babu was Police Superintendent."),
                    LiveMockQuestionItem("ben_m5", "‘অভিষেক’ কাব্যাংশে ইন্দ্রজিতের স্ত্রীর নাম কী?", "What was Indrajit's wife's name?", listOf("প্রমীলা", "মন্দোদরী", "চিত্রাঙ্গদা", "উর্মিলা"), listOf("Pramila", "Mandodari", "Chitrangada", "Urmila"), 0, "মেঘনাদ বা ইন্দ্রজিতের বীরপত্নী ছিলেন প্রমীলা।", "Indrajit's wife was Pramila.")
                )
            ),
            LiveMockArena(
                id = "english",
                titleBengali = "ইংরেজি লাইভ মক টেস্ট",
                titleEnglish = "English Live Mock Test",
                subtitleBengali = "Grammar, Vocabulary & Comprehension",
                subtitleEnglish = "Textual & Functional English",
                icon = Icons.Default.Translate,
                primaryColor = Color(0xFF2563EB),
                preNumber = 580,
                durationMinutes = 60,
                questionCount = 60,
                questions = listOf(
                    LiveMockQuestionItem("eng_m1", "Who is the author of 'Father's Help'?", "Who wrote 'Father's Help'?", listOf("R.K. Narayan", "Ruskin Bond", "Liam O'Flaherty", "Ralph Waldo Emerson"), listOf("R.K. Narayan", "Ruskin Bond", "Liam O'Flaherty", "Ralph Waldo Emerson"), 0, "'Father's Help' is written by the celebrated Indian author R.K. Narayan.", "Written by R.K. Narayan."),
                    LiveMockQuestionItem("eng_m2", "What is the synonym of 'Frightened'?", "What is the synonym of 'Frightened'?", listOf("Terrified", "Joyful", "Brave", "Curious"), listOf("Terrified", "Joyful", "Brave", "Curious"), 0, "'Terrified' means intensely scared or frightened.", "Terrified means frightened."),
                    LiveMockQuestionItem("eng_m3", "Change into indirect speech: He said, 'I am reading.'", "Change into indirect speech: He said, 'I am reading.'", listOf("He said that he was reading.", "He said that he is reading.", "He told he had read.", "He says he was reading."), listOf("He said that he was reading.", "He said that he is reading.", "He told he had read.", "He says he was reading."), 0, "Present continuous changes to past continuous in reported speech.", "Changes to past continuous."),
                    LiveMockQuestionItem("eng_m4", "In 'The Cat', the cat considers food from the guest as:", "In 'The Cat', guest's gift is:", listOf("His rightful due", "A great favour", "An accident", "Unwanted"), listOf("His rightful due", "A great favour", "An accident", "Unwanted"), 0, "The cat puts in an appearance to get his share as a rightful due.", "Considered rightful due."),
                    LiveMockQuestionItem("eng_m5", "Choose the correct preposition: He is senior ___ me by two years.", "Choose correct preposition: He is senior ___ me.", listOf("to", "than", "from", "with"), listOf("to", "than", "from", "with"), 0, "Words like senior, junior, superior, inferior take the preposition 'to'.", "Senior takes 'to'.")
                )
            ),
            LiveMockArena(
                id = "history",
                titleBengali = "ইতিহাস লাইভ মক টেস্ট",
                titleEnglish = "History Live Mock Test",
                subtitleBengali = "ভারতের জাতীয় আন্দোলন ও সমাজ সংস্কার",
                subtitleEnglish = "Modern India & National Movement",
                icon = Icons.Default.AccountBalance,
                primaryColor = Color(0xFFD97706),
                preNumber = 490,
                durationMinutes = 60,
                questionCount = 60,
                questions = listOf(
                    LiveMockQuestionItem("hist_m1", "‘সোমপ্রকাশ’ পত্রিকার সম্পাদক কে ছিলেন?", "Who edited Somprakash?", listOf("দ্বারকানাথ বিদ্যাভূষণ", "ঈশ্বরচন্দ্র বিদ্যাসাগর", "হরিশচন্দ্র মুখোপাধ্যায়", "অক্ষয়কুমার দত্ত"), listOf("Dwarkanath Vidyabhusan", "Vidyasagar", "Harish Chandra", "Akshay Kumar"), 0, "‘সোমপ্রকাশ’ পত্রিকা ১৮৫৮ সালে দ্বারকানাথ বিদ্যাভূষণের সম্পাদনায় প্রকাশিত হয়।", "Edited by Dwarkanath Vidyabhusan."),
                    LiveMockQuestionItem("hist_m2", "ভারতের প্রথম জাতীয় অরণ্য আইন কবে পাস হয়?", "When was the first Indian Forest Act passed?", listOf("১৮৬৫ খ্রিষ্টাব্দে", "১৮৭৮ খ্রিষ্টাব্দে", "১৮৫৭ খ্রিষ্টাব্দে", "১৯২৭ খ্রিষ্টাব্দে"), listOf("1865 AD", "1878 AD", "1857 AD", "1927 AD"), 0, "ভারতে ব্রিটিশ সরকার ১৮৬৫ সালে প্রথম অরণ্য আইন জারি করে।", "Passed in 1865."),
                    LiveMockQuestionItem("hist_m3", "সাঁওতাল বিদ্রোহের দুজন প্রধান নেতার নাম কী?", "Who were leaders of Santhal rebellion?", listOf("সিধু ও কানু", "বিরসা ও গয়া মুন্ডা", "তিতুমীর ও গোলাম মাসুম", "দিগম্বর ও বিষ্ণুচরণ"), listOf("Sidhu & Kanhu", "Birsa & Gaya", "Titumir & Masum", "Digambar & Bishnu"), 0, "১৮৫৫ সালে সাঁওতাল বিদ্রোহের মূল কাণ্ডারী ছিলেন সিধু ও কানু।", "Led by Sidhu and Kanhu."),
                    LiveMockQuestionItem("hist_m4", "‘বর্তমান ভারত’ গ্রন্থটি কার রচনা?", "Who wrote 'Bartaman Bharat'?", listOf("স্বামী বিবেকানন্দ", "বঙ্কিমচন্দ্র চট্টোপাধ্যায়", "রবীন্দ্রনাথ ঠাকুর", "অরবিন্দ ঘোষ"), listOf("Swami Vivekananda", "Bankim Chandra", "Tagore", "Aurobindo"), 0, "‘বর্তমান ভারত’ স্বামী বিবেকানন্দের একটি ঐতিহাসিক দেশাত্মবোধক গ্রন্থ।", "Written by Swami Vivekananda."),
                    LiveMockQuestionItem("hist_m5", "কলকাতায় এশিয়াটিক সোসাইটি কে প্রতিষ্ঠা করেন?", "Who founded Asiatic Society in Calcutta?", listOf("স্যার উইলিয়াম জোন্স", "লর্ড ওয়ারেন হেস্টিংস", "ডেভিড হেয়ার", "উইলিয়াম কেরি"), listOf("Sir William Jones", "Warren Hastings", "David Hare", "William Carey"), 0, "১৭৮৪ খ্রিস্টাব্দে স্যার উইলিয়াম জোন্স এশিয়াটিক সোসাইটি প্রতিষ্ঠা করেন।", "Founded by Sir William Jones in 1784.")
                )
            ),
            LiveMockArena(
                id = "geography",
                titleBengali = "ভূগোল লাইভ মক টেস্ট",
                titleEnglish = "Geography Live Mock Test",
                subtitleBengali = "প্রাকৃতিক ও ভারতের অর্থনৈতিক ভূগোল",
                subtitleEnglish = "Physical & Regional Geography",
                icon = Icons.Default.Public,
                primaryColor = Color(0xFF0D9488),
                preNumber = 430,
                durationMinutes = 60,
                questionCount = 60,
                questions = listOf(
                    LiveMockQuestionItem("geo_m1", "বায়ুমণ্ডলের কোন স্তরে উল্কাপিণ্ড পুড়ে ছাই হয়ে যায়?", "In which atmospheric layer do meteors burn up?", listOf("মেসোস্ফিয়ার", "ট্রপোস্ফিয়ার", "স্ট্র্যাটোস্ফিয়ার", "থার্মোস্ফিয়ার"), listOf("Mesosphere", "Troposphere", "Stratosphere", "Thermosphere"), 0, "মেসোস্ফিয়ারের প্রচণ্ড ঠাণ্ডা ও বায়ুকণার ঘর্ষণে উল্কাপিণ্ড পুড়ে ছাই হয়।", "Meteors burn in Mesosphere."),
                    LiveMockQuestionItem("geo_m2", "ভারতের দীর্ঘতম জাতীয় সড়ক কোনটি?", "Which is the longest National Highway in India?", listOf("NH 44", "NH 2", "NH 6", "NH 19"), listOf("NH 44", "NH 2", "NH 6", "NH 19"), 0, "শ্রীনগর থেকে কন্যাকুমারী পর্যন্ত বিস্তৃত NH 44 ভারতের দীর্ঘতম সড়ক।", "NH 44 is the longest."),
                    LiveMockQuestionItem("geo_m3", "কোন নদীকে ‘দক্ষিণ ভারতের গঙ্গা’ বলা হয়?", "Which river is called 'Ganga of South India'?", listOf("গোদাবরী", "কৃষ্ণা", "কাবেরী", "নর্মদা"), listOf("Godavari", "Krishna", "Kaveri", "Narmada"), 0, "দৈর্ঘ্য ও প্রাচীনত্বের জন্য গোদাবরীকে দক্ষিণ ভারতের গঙ্গা বা বৃদ্ধগঙ্গা বলা হয়।", "Godavari is Ganga of South."),
                    LiveMockQuestionItem("geo_m4", "মরু অঞ্চলে ক্ষয়কার্যের ফলে সৃষ্ট অবশিষ্ট পাহাড়কে কী বলে?", "What is an isolated residual hill in desert called?", listOf("ইনসেলবার্জ", "ইয়ারদাং", "জিউগেন", "বার্খান"), listOf("Inselberg", "Yardang", "Zeugen", "Barchan"), 0, "মরু অঞ্চলে কঠিন শিলাগঠিত অবশিষ্ট টিলা বা পাহাড়কে ইনসেলবার্জ বলা হয়।", "Isolated hill is Inselberg."),
                    LiveMockQuestionItem("geo_m5", "ভারতের ম্যানচেস্টার নামে কোন শহর পরিচিত?", "Which city is known as Manchester of India?", listOf("আহমেদাবাদ", "মুম্বাই", "কোলকাতা", "কোয়েম্বাটোর"), listOf("Ahmedabad", "Mumbai", "Kolkata", "Coimbatore"), 0, "কার্পাস বস্ত্রবয়ন শিল্পের জন্য আহমেদাবাদকে ভারতের ম্যানচেস্টার বলা হয়।", "Ahmedabad is Manchester of India.")
                )
            ),
            LiveMockArena(
                id = "life_science",
                titleBengali = "জীবন বিজ্ঞান লাইভ মক টেস্ট",
                titleEnglish = "Life Science Live Mock Test",
                subtitleBengali = "জীবজগতে নিয়ন্ত্রণ, বংশগতি ও পরিবেশ",
                subtitleEnglish = "Control, Genetics & Environment",
                icon = Icons.Default.Biotech,
                primaryColor = Color(0xFF059669),
                preNumber = 520,
                durationMinutes = 60,
                questionCount = 60,
                questions = listOf(
                    LiveMockQuestionItem("life_m1", "মানবদেহের ভারসাম্য নিয়ন্ত্রণে মস্তিষ্কের কোন অংশ সাহায্য করে?", "Which part of brain controls body balance?", listOf("লঘুমস্তিষ্ক (Cerebellum)", "গুরুমস্তিষ্ক", "মেডুলা অবলংগাটা", "থ্যালামাস"), listOf("Cerebellum", "Cerebrum", "Medulla Oblongata", "Thalamus"), 0, "লঘুমস্তিষ্ক বা সেরিবেলাম দেহের ভারসাম্য রক্ষা করতে কাজ করে।", "Cerebellum maintains balance."),
                    LiveMockQuestionItem("life_m2", "ডিএনএ-এর দ্বি-তন্ত্রী কাঠামোর মডেল কে আবিষ্কার করেন?", "Who discovered the DNA double helix model?", listOf("ওয়াটসন ও ক্রিক", "মেন্ডেল", "ডারউইন", "রবার্ট হুক"), listOf("Watson & Crick", "Mendel", "Darwin", "Robert Hooke"), 0, "১৯৫৩ সালে জেমস ওয়াটসন ও ফ্রান্সিস ক্রিক ডিএনএ ডাবল হেলিক্স আবিষ্কার করেন।", "Watson & Crick discovered DNA helix."),
                    LiveMockQuestionItem("life_m3", "কোন হরমোনকে আপাতকালীন বা সংকটকালীন হরমোন বলা হয়?", "Which hormone is called emergency hormone?", listOf("অ্যাড্রেনালিন", "থাইরক্সিন", "ইনসুলিন", "গ্লুকাগন"), listOf("Adrenaline", "Thyroxine", "Insulin", "Glucagon"), 0, "ভয় বা উত্তেজনার সময় রক্তে দ্রুত ক্ষরিত হয়ে দেহকে প্রস্তুত করে অ্যাড্রেনালিন।", "Adrenaline is emergency hormone."),
                    LiveMockQuestionItem("life_m4", "মটর গাছের ক্ষেত্রে কোনটি প্রকট বৈশিষ্ট্য?", "Which is dominant trait in pea plant?", listOf("বেগুনি ফুল", "সাদা ফুল", "কুঞ্চিত বীজ", "খর্বাকার কাণ্ড"), listOf("Purple flower", "White flower", "Wrinkled seed", "Dwarf stem"), 0, "মেন্ডেলের গবেষণায় বেগুনি ফুল ছিল ফুলের রঙের প্রকট বৈশিষ্ট্য।", "Purple flower is dominant."),
                    LiveMockQuestionItem("life_m5", "সুন্দরবনের প্রধান বাঘের প্রজাতি কোনটি?", "Which tiger species is found in Sundarbans?", listOf("রয়্যাল বেঙ্গল টাইগার", "সাইবেরিয়ান টাইগার", "সুমাত্রান টাইগার", "কাস্পিয়ান টাইগার"), listOf("Royal Bengal Tiger", "Siberian Tiger", "Sumatran Tiger", "Caspian Tiger"), 0, "সুন্দরবনের ম্যানগ্রোভ অরণ্যে বিখ্যাত রয়্যাল বেঙ্গল টাইগার বসবাস করে।", "Royal Bengal Tiger.")
                )
            ),
            LiveMockArena(
                id = "physical_science",
                titleBengali = "ভৌত বিজ্ঞান লাইভ মক টেস্ট",
                titleEnglish = "Physical Science Live Mock Test",
                subtitleBengali = "পদার্থবিদ্যা, রসায়ন ও পরিবেশগত ভাবনা",
                subtitleEnglish = "Physics & Chemistry",
                icon = Icons.Default.Science,
                primaryColor = Color(0xFF7C3AED),
                preNumber = 560,
                durationMinutes = 60,
                questionCount = 60,
                questions = listOf(
                    LiveMockQuestionItem("psc_m1", "বয়েলের সূত্রে কোন ভৌত রাশিটি ধ্রুবক থাকে?", "Which quantity remains constant in Boyle's law?", listOf("তাপমাত্রা ও গ্যাসের ভর", "চাপ ও তাপমাত্রা", "কেবলমাত্র আয়তন", "কেবলমাত্র চাপ"), listOf("Temperature & Mass", "Pressure & Temp", "Only volume", "Only pressure"), 0, "নির্দিষ্ট ভরের গ্যাসের ক্ষেত্রে উষ্ণতা স্থির থাকলে চাপ আয়তনের ব্যস্তানুপাতিক।", "Temperature & Mass remain constant."),
                    LiveMockQuestionItem("psc_m2", "আলোর কোন ধর্মের জন্য মরুভূমিতে মরীচিকা সৃষ্টি হয়?", "Which property of light causes mirage in desert?", listOf("অভ্যন্তরীণ পূর্ণ প্রতিফলন", "নিয়মিত প্রতিফলন", "বিচ্ছুরণ", "প্রতিসরণ"), listOf("Total Internal Reflection", "Regular reflection", "Dispersion", "Refraction"), 0, "বায়ুস্তরের উষ্ণতা পার্থক্যের ফলে আলোকরশ্মির অভ্যন্তরীণ পূর্ণ প্রতিফলনে মরীচিকা তৈরি হয়।", "Caused by Total Internal Reflection."),
                    LiveMockQuestionItem("psc_m3", "তড়িৎ আধানের এস.আই (S.I.) একক কোনটি?", "What is the SI unit of electric charge?", listOf("কুলম্ব (Coulomb)", "অ্যাম্পিয়ার", "ভোল্ট", "ওহম"), listOf("Coulomb", "Ampere", "Volt", "Ohm"), 0, "তড়িৎ আধানের আন্তর্জাতিক একক কুলম্ব (C)।", "SI unit is Coulomb."),
                    LiveMockQuestionItem("psc_m4", "আধুনিক পর্যায় সারণীতে মৌলসমূহ কিসের ভিত্তিতে সাজানো?", "Modern periodic table is based on:", listOf("পারমাণবিক ক্রমাঙ্ক (Atomic Number)", "পারমাণবিক ভর", "যোজ্যতা", "ঘনত্ব"), listOf("Atomic Number", "Atomic Mass", "Valency", "Density"), 0, "মোজলের সূত্রানুসারে আধুনিক পর্যায় সারণী পারমাণবিক সংখ্যার ক্রমানুসারে গঠিত।", "Based on Atomic Number."),
                    LiveMockQuestionItem("psc_m5", "মার্শ গ্যাসের প্রধান উপাদান কোনটি?", "What is the main component of Marsh Gas?", listOf("মিথেন (CH4)", "ইথেন", "প্রোপেন", "বিউটেন"), listOf("Methane (CH4)", "Ethane", "Propane", "Butane"), 0, "জলাভূমিতে জৈব পদার্থের পচনে মিথেন বা মার্শ গ্যাস উৎপন্ন হয়।", "Methane is marsh gas.")
                )
            ),
            LiveMockArena(
                id = "mathematics",
                titleBengali = "গণিত লাইভ মক টেস্ট",
                titleEnglish = "Mathematics Live Mock Test",
                subtitleBengali = "বীজগণিত, পাটিগণিত, জ্যামিতি ও পরিমিতি",
                subtitleEnglish = "Algebra, Geometry & Arithmetic",
                icon = Icons.Default.Calculate,
                primaryColor = Color(0xFFDC2626),
                preNumber = 600,
                durationMinutes = 60,
                questionCount = 60,
                questions = listOf(
                    LiveMockQuestionItem("math_m1", "একটি দ্বিঘাত সমীকরণের সর্বোচ্চ ক'টি বীজ থাকতে পারে?", "Max roots of a quadratic equation?", listOf("২টি", "১টি", "৩টি", "অসংখ্য"), listOf("2", "1", "3", "Infinite"), 0, "যেকোনো দ্বিঘাত সমীকরণের ঠিক দুটি বীজ থাকে।", "A quadratic equation has 2 roots."),
                    LiveMockQuestionItem("math_m2", "একটি বৃত্তের ব্যাসার্ধ r হলে, তার ক্ষেত্রফল কত?", "If radius is r, what is the area of a circle?", listOf("πr²", "২πr", "৪/৩ πr³", "২πr²"), listOf("πr²", "2πr", "4/3 πr³", "2πr²"), 0, "বৃত্তের ক্ষেত্রফল নির্ণয়ের সূত্র πr² বর্গ একক।", "Area of circle is πr²."),
                    LiveMockQuestionItem("math_m3", "sin²θ + cos²θ এর মান কত?", "What is the value of sin²θ + cos²θ?", listOf("১", "০", "-১", "২"), listOf("1", "0", "-1", "2"), 0, "ত্রিকোণমিতিক মৌলিক অভেদ অনুযায়ী sin²θ + cos²θ = ১।", "Identity sin²θ + cos²θ = 1."),
                    LiveMockQuestionItem("math_m4", "বার্ষিক r% সরল সুদে p টাকার t বছরের সুদ কত?", "Simple interest of P at r% for t years?", listOf("Prt / ১০০", "Pr / ১০০t", "১০০ / Prt", "P / rt"), listOf("Prt / 100", "Pr / 100t", "100 / Prt", "P / rt"), 0, "সরল সুদের সূত্র I = Prt / ১০০।", "I = Prt / 100."),
                    LiveMockQuestionItem("math_m5", "একটি ঘনকের মোট বাহুর সংখ্যা কয়টি?", "Total number of edges in a cube?", listOf("১২টি", "৬টি", "৮টি", "১০টি"), listOf("12", "6", "8", "10"), 0, "একটি ঘনকের ৬টি তল, ৮টি শীর্ষবিন্দু ও ১২টি ধার বা বাহু থাকে।", "A cube has 12 edges.")
                )
            ),
            LiveMockArena(
                id = "mega_grand",
                titleBengali = "অল-বেঙ্গল মেগা মক টেস্ট ২০২৬",
                titleEnglish = "All-Bengal Mega Grand Mock Test",
                subtitleBengali = "সব বিষয়ের সমন্বয়ে পূর্ণাঙ্গ মাধ্যমিক প্রস্তুতি",
                subtitleEnglish = "Combined State-level Mock Examination",
                icon = Icons.Default.MilitaryTech,
                primaryColor = Color(0xFFD97706),
                preNumber = 950,
                durationMinutes = 60,
                questionCount = 60,
                questions = listOf(
                    LiveMockQuestionItem("mega_m1", "ভারতের সংবিধানের জনক কাকে বলা হয়?", "Who is father of Indian Constitution?", listOf("ড. বি. আর. আম্বেদকর", "মহাত্মা গান্ধী", "জওহরলাল নেহেরু", "ড. রাজেন্দ্র প্রসাদ"), listOf("Dr. B.R. Ambedkar", "Mahatma Gandhi", "Nehru", "Rajendra Prasad"), 0, "ড. বি. আর. আম্বেদকর ছিলেন খসড়া কমিটির সভাপতি ও সংবিধানের জনক।", "Dr. B.R. Ambedkar."),
                    LiveMockQuestionItem("mega_m2", "গাছের সবুজ পাতার রঞ্জক পদার্থটির নাম কী?", "Green pigment of plant leaf?", listOf("ক্লোরোফিল", "হিমোগ্লোবিন", "ক্যারোটিন", "মেলানিন"), listOf("Chlorophyll", "Hemoglobin", "Carotene", "Melanin"), 0, "ক্লোরোফিলের কারণে উদ্ভিদের পাতা সবুজ হয় এবং সালোকসংশ্লেষ ঘটে।", "Chlorophyll."),
                    LiveMockQuestionItem("mega_m3", "সূর্যের আলো পৃথিবী পর্যন্ত পৌঁছাতে আনুমানিক কত সময় লাগে?", "Time taken by sunlight to reach earth?", listOf("৮ মিনিট ২০ সেকেন্ড", "৫ মিনিট", "১০ মিনিট", "২ মিনিট"), listOf("8 min 20 sec", "5 min", "10 min", "2 min"), 0, "আলোর গতিবেগ ৩ লক্ষ কিমি/সেকেন্ড অনুযায়ী সময় লাগে প্রায় ৮ মিনিট ২০ সেকেন্ড।", "Approx 8 min 20 sec."),
                    LiveMockQuestionItem("mega_m4", "পশ্চিমবঙ্গের রাজ্য পশুর নাম কী?", "State animal of West Bengal?", listOf("মেছো বিড়াল (Fishing Cat)", "রয়েল বেঙ্গল টাইগার", "একশৃঙ্গ গণ্ডার", "হাতি"), listOf("Fishing Cat", "Royal Bengal Tiger", "One-horned Rhino", "Elephant"), 0, "পশ্চিমবঙ্গের রাজ্য পশু হলো মেছো বিড়াল বা বাঘরোল।", "Fishing cat is state animal."),
                    LiveMockQuestionItem("mega_m5", "কোন বিজ্ঞানী বংশগতির সূত্র আবিষ্কার করেন?", "Who discovered laws of inheritance?", listOf("গ্রেগর জোহান মেন্ডেল", "চার্লস ডারউইন", "ল্যামার্ক", "লুই পাস্তুর"), listOf("Gregor Johann Mendel", "Darwin", "Lamarck", "Pasteur"), 0, "অস্ট্রিয়ার ধর্মযাজক গ্রেগর জোহান মেন্ডেল বংশগতির জনক।", "Mendel is father of genetics.")
                )
            )
        )
    }

    var selectedArena by remember { mutableStateOf<LiveMockArena?>(null) }
    var gameState by remember { mutableStateOf("LOBBY") } // LOBBY, DETAILS, COUNTDOWN, TEST, RESULT

    // Search query in lobby
    var searchQuery by remember { mutableStateOf("") }

    // Pulsing live dot animation
    val infiniteTransition = rememberInfiniteTransition(label = "mock_live_dot")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mock_dot_alpha"
    )

    // Animated border colors (Flowing RGB animation)
    val borderTransition = rememberInfiniteTransition(label = "border_anim")
    val progress by borderTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "progress"
    )
    val cTop = Color.hsl((progress * 360f), 1f, 0.5f)
    val cRight = Color.hsl(((progress + 0.25f) * 360f) % 360f, 1f, 0.5f)
    val cBottom = Color.hsl(((progress + 0.5f) * 360f) % 360f, 1f, 0.5f)
    val cLeft = Color.hsl(((progress + 0.75f) * 360f) % 360f, 1f, 0.5f)

    // Dynamic fluctuating online player count per arena (50 Minimum, 100 Maximum added to Pre-Number!)
    val onlineCountsMap = remember {
        mutableStateMapOf<String, Int>().apply {
            mockArenas.forEach { arena ->
                put(arena.id, (55..85).random())
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1200L) // Smooth fluctuation every 1.2s
            mockArenas.forEach { arena ->
                if (kotlin.random.Random.nextFloat() < 0.70f) {
                    val current = onlineCountsMap[arena.id] ?: 75
                    val delta = (-2..3).random()
                    val nextCount = (current + delta).coerceIn(50, 100)
                    onlineCountsMap[arena.id] = nextCount
                }
            }
        }
    }

    fun formatBengaliDigits(num: Int): String {
        val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return num.toString().map { if (it in '0'..'9') bengaliDigits[it - '0'] else it }.joinToString("")
    }

    // Helper to build 60 questions for the selected mock arena
    fun buildMockTestPool(
        arenaId: String,
        curatedFallback: List<LiveMockQuestionItem>,
        allDbQuestions: List<QuestionEntity>,
        targetClassId: String,
        targetCount: Int = 60
    ): List<LiveMockQuestionItem> {
        val matchingEntities = allDbQuestions.filter { q ->
            val matchesClass = q.classId == targetClassId
            val matchesSubject = when (arenaId) {
                "bengali" -> q.subjectId.contains("ben", ignoreCase = true) || q.subjectId.contains("বাংলা", ignoreCase = true)
                "english" -> q.subjectId.contains("eng", ignoreCase = true) || q.subjectId.contains("ইংরেজি", ignoreCase = true)
                "history" -> q.subjectId.contains("hist", ignoreCase = true) || q.subjectId.contains("ইতিহাস", ignoreCase = true)
                "geography" -> q.subjectId.contains("geo", ignoreCase = true) || q.subjectId.contains("ভূগোল", ignoreCase = true)
                "life_science" -> q.subjectId.contains("life", ignoreCase = true) || q.subjectId.contains("bio", ignoreCase = true) || q.subjectId.contains("জীবন", ignoreCase = true)
                "physical_science" -> q.subjectId.contains("phy", ignoreCase = true) || q.subjectId.contains("chem", ignoreCase = true) || q.subjectId.contains("ভৌত", ignoreCase = true)
                "mathematics" -> q.subjectId.contains("math", ignoreCase = true) || q.subjectId.contains("গণিত", ignoreCase = true)
                else -> true
            }
            matchesClass && matchesSubject
        }

        val convertedFromDb = matchingEntities.mapNotNull { entity ->
            val rawOptions = if (entity.optionsJson.contains("|")) {
                entity.optionsJson.split("|").map { it.trim() }.filter { it.isNotBlank() }
            } else if (entity.optionsJson.contains(",")) {
                entity.optionsJson.split(",").map { it.trim().removeSurrounding("\"").removeSurrounding("[").removeSurrounding("]") }.filter { it.isNotBlank() }
            } else {
                emptyList()
            }
            if (rawOptions.size >= 2) {
                val safeIdx = entity.correctOptionIndex.coerceIn(0, rawOptions.size - 1)
                LiveMockQuestionItem(
                    id = entity.id,
                    questionBengali = entity.question,
                    questionEnglish = entity.question,
                    optionsBengali = rawOptions,
                    optionsEnglish = rawOptions,
                    correctIndex = safeIdx,
                    explanationBengali = entity.explanation.ifBlank { "সঠিক উত্তর: ${rawOptions[safeIdx]}" },
                    explanationEnglish = entity.explanation.ifBlank { "Correct Answer: ${rawOptions[safeIdx]}" }
                )
            } else null
        }

        val totalSubjectPool = (convertedFromDb + curatedFallback).distinctBy { it.questionBengali.trim().lowercase() }
        val pool = if (totalSubjectPool.isNotEmpty()) totalSubjectPool else curatedFallback
        if (pool.isEmpty()) return curatedFallback

        // Build exactly targetCount (60) questions
        val result = mutableListOf<LiveMockQuestionItem>()
        var cycle = 1
        while (result.size < targetCount) {
            val shuffledBatch = pool.shuffled()
            for (item in shuffledBatch) {
                if (result.size >= targetCount) break
                result.add(
                    if (cycle == 1) item
                    else item.copy(id = "${item.id}_c$cycle")
                )
            }
            cycle++
        }
        return result
    }

    // Default authentic top rankers for each subject arena (out of 60)
    fun getDefaultTopRankers(arenaId: String, maxMarks: Int = 60): List<LiveMockLeaderboardUser> {
        val scoreSpread = listOf(60, 59, 58, 57, 56, 55, 54, 52, 50, 48)
        val namesAndDistricts = listOf(
            "অনিন্দিতা দাস" to "Kolkata",
            "সৌম্যদীপ ব্যানার্জি" to "Hooghly",
            "শুভ্রা পাল" to "Siliguri",
            "রাহুল সেনগুপ্ত" to "Howrah",
            "অঙ্কিতা মণ্ডল" to "North 24 Pgs",
            "রোহিত বিশ্বাস" to "Nadia",
            "প্রিয়া ভৌমিক" to "Burdwan",
            "দেবজিৎ চক্রবর্তী" to "Bankura",
            "সুস্মিতা পাল" to "Medinipur",
            "স্নেহা রায়" to "Malda"
        )
        val colors = listOf(
            Color(0xFF2563EB), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF8B5CF6),
            Color(0xFFEC4899), Color(0xFF06B6D4), Color(0xFF14B8A6), Color(0xFF6366F1),
            Color(0xFF84CC16), Color(0xFFF97316)
        )
        return namesAndDistricts.mapIndexed { idx, pair ->
            LiveMockLeaderboardUser(
                name = pair.first,
                district = pair.second,
                score = scoreSpread.getOrElse(idx) { 45 },
                maxScore = maxMarks,
                avatarColor = colors.getOrElse(idx) { Color(0xFF2563EB) }
            )
        }
    }

    val arenaUserScoreMap = remember { mutableStateMapOf<String, Int>() }

    fun getArenaTop10(arenaId: String, currentScore: Int? = null, maxMarks: Int = 60): List<LiveMockLeaderboardUser> {
        val defaultList = getDefaultTopRankers(arenaId, maxMarks)
        val scoreToUse = currentScore ?: arenaUserScoreMap[arenaId]
        return if (scoreToUse != null) {
            val displayName = studentProfile?.name?.takeIf { it.isNotBlank() } ?: "আপনি"
            val userItem = LiveMockLeaderboardUser(
                name = displayName,
                district = "আপনার স্কোর",
                score = scoreToUse,
                maxScore = maxMarks,
                avatarColor = PrimaryIndigo,
                isUser = true
            )
            (defaultList + userItem)
                .sortedWith(
                    compareByDescending<LiveMockLeaderboardUser> { it.score }
                        .thenBy { !it.isUser }
                )
                .take(10)
        } else {
            defaultList.take(10)
        }
    }

    fun getUserRankInArena(arenaId: String, score: Int, maxMarks: Int = 60): Int {
        val defaultList = getDefaultTopRankers(arenaId, maxMarks)
        val higherScoresCount = defaultList.count { it.score > score }
        return higherScoresCount + 1
    }

    // Active test state
    var activeTestQuestions by remember { mutableStateOf<List<LiveMockQuestionItem>>(emptyList()) }
    var currentQuestionIdx by remember { mutableIntStateOf(0) }
    val userSelectedAnswers = remember { mutableStateMapOf<Int, Int>() } // questionIndex -> optionIndex
    var testTimeRemainingSec by remember { mutableIntStateOf(15 * 60) }
    var countdownSec by remember { mutableIntStateOf(3) }
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }
    var hasRewardedBonus by remember { mutableStateOf(false) }

    // Start countdown
    LaunchedEffect(gameState, selectedArena) {
        if (gameState == "COUNTDOWN") {
            val arena = selectedArena ?: mockArenas[0]
            val questions = buildMockTestPool(arena.id, arena.questions, dbQuestions, studentProfile?.classId ?: "class_10_wbbse", arena.questionCount)
            activeTestQuestions = if (questions.isNotEmpty()) questions else arena.questions
            userSelectedAnswers.clear()
            currentQuestionIdx = 0
            testTimeRemainingSec = arena.durationMinutes * 60
            hasRewardedBonus = false

            countdownSec = 3
            while (countdownSec > 0) {
                delay(1000)
                countdownSec--
            }
            gameState = "TEST"
        }
    }

    // Live overall countdown timer for the full test
    LaunchedEffect(gameState) {
        if (gameState == "TEST") {
            while (testTimeRemainingSec > 0 && gameState == "TEST") {
                delay(1000)
                testTimeRemainingSec--
            }
            if (testTimeRemainingSec <= 0 && gameState == "TEST") {
                // Auto-submit when time expires
                gameState = "RESULT"
            }
        }
    }

    // Results calculation
    val totalQuestions = activeTestQuestions.size.coerceAtLeast(1)
    val correctCount = activeTestQuestions.indices.count { idx ->
        userSelectedAnswers[idx] == activeTestQuestions[idx].correctIndex
    }
    val wrongCount = activeTestQuestions.indices.count { idx ->
        val selected = userSelectedAnswers[idx]
        selected != null && selected != activeTestQuestions[idx].correctIndex
    }
    val unattemptedCount = totalQuestions - userSelectedAnswers.size

    // Save reward upon completion
    LaunchedEffect(gameState) {
        if (gameState == "RESULT" && !hasRewardedBonus) {
            hasRewardedBonus = true
            val arenaId = selectedArena?.id ?: "bengali"
            val prevBest = arenaUserScoreMap[arenaId] ?: 0
            if (correctCount > prevBest) {
                arenaUserScoreMap[arenaId] = correctCount
            }
            val earnedXp = (correctCount * 15) + 100 // XP bonus
            val earnedCoins = (correctCount * 5) + 30
            viewModel?.addRewardBonus(earnedXp, earnedCoins)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (gameState) {
                            "LOBBY" -> "লাইভ মক টেস্ট হল"
                            "DETAILS" -> selectedArena?.let { if (isBengali) it.titleBengali else it.titleEnglish } ?: ("লাইভ মক টেস্ট")
                            "TEST" -> selectedArena?.let { if (isBengali) it.titleBengali else it.titleEnglish } ?: ("লাইভ মক টেস্ট")
                            else -> "মক টেস্ট রেজাল্ট"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    // LIVE indicator badge with animated pulsing red dot
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .graphicsLayer { alpha = dotAlpha }
                                    .background(Color(0xFFEF4444), CircleShape)
                            )
                            Text(
                                text = "লাইভ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            when (gameState) {
                                "LOBBY" -> onBack()
                                "DETAILS" -> gameState = "LOBBY"
                                "TEST" -> showSubmitConfirmDialog = true
                                "RESULT" -> gameState = "LOBBY"
                                else -> gameState = "LOBBY"
                            }
                        },
                        modifier = Modifier.testTag("live_mock_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (gameState) {
                "LOBBY" -> {
                    // Lobby Screen: Search + Live Subject Arenas
                    val filteredArenas = mockArenas.filter { arena ->
                        searchQuery.isBlank() ||
                                arena.titleBengali.contains(searchQuery, ignoreCase = true) ||
                                arena.titleEnglish.contains(searchQuery, ignoreCase = true) ||
                                arena.subtitleBengali.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Hero Header Banner
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = PrimaryIndigo.copy(alpha = 0.08f)),
                                border = BorderStroke(1.dp, PrimaryIndigo.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = PrimaryIndigo.copy(alpha = 0.18f),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Timer,
                                                contentDescription = null,
                                                tint = PrimaryIndigo,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "অল-বেঙ্গল লাইভ মক টেস্ট ২০২৬",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "আসল পরীক্ষার সময় অনুযায়ী ১৫ মিনিটের পূর্ণাঙ্গ টেস্ট",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Search Input
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text("মক টেস্ট বা বিষয় খুঁজুন...")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("live_mock_search_input")
                            )
                        }

                        // Section Title
                        item {
                            Text(
                                text = "উপলব্ধ লাইভ মক টেস্ট এরিনা (${filteredArenas.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Arenas list with distinct Pre-Numbers and fluctuating 50..100 online counts!
                        items(filteredArenas) { arena ->
                            val preNumber = arena.preNumber
                            val liveFluctuation = onlineCountsMap[arena.id] ?: 75
                            val totalOnline = preNumber + liveFluctuation

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, arena.primaryColor.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedArena = arena
                                        gameState = "DETAILS"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Subject Icon
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = arena.primaryColor.copy(alpha = 0.15f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = arena.icon,
                                                contentDescription = null,
                                                tint = arena.primaryColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Details
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isBengali) arena.titleBengali else arena.titleEnglish,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isBengali) arena.subtitleBengali else arena.subtitleEnglish,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "${formatBengaliDigits(arena.questionCount)}টি প্রশ্ন • ${formatBengaliDigits(arena.durationMinutes)} মিনিট",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(1.dp))

                                        // Animated Live Counter
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .graphicsLayer { alpha = dotAlpha.coerceAtLeast(0.5f) }
                                                    .background(Color(0xFFEF4444), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${formatBengaliDigits(totalOnline)} জন পরীক্ষার্থী লাইভ",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = arena.primaryColor
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Compact Start Button
                                    Button(
                                        onClick = {
                                            selectedArena = arena
                                            gameState = "DETAILS"
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = arena.primaryColor),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(
                                            text = "টেস্ট দিন",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "DETAILS" -> {
                    // Arena Details, Rules & Top 10 Live Merit List Screen
                    val arena = selectedArena ?: mockArenas[0]
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Surface(
                                shape = CircleShape,
                                color = arena.primaryColor.copy(alpha = 0.18f),
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = arena.icon,
                                        contentDescription = null,
                                        tint = arena.primaryColor,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                        }

                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isBengali) arena.titleBengali else arena.titleEnglish,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${formatBengaliDigits(arena.questionCount)}টি পূর্ণাঙ্গ প্রশ্ন • ${formatBengaliDigits(arena.durationMinutes)} মিনিট পরীক্ষা",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Rules Card
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "নিয়মাবলী ও নম্বর বিভাজন:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        RuleItem(
                                            icon = Icons.Default.Timer,
                                            title = "${formatBengaliDigits(arena.durationMinutes)} মিনিট",
                                            subtitle = "সামগ্রিক টেস্ট সময়",
                                            color = Color(0xFFF59E0B),
                                            modifier = Modifier.weight(1f)
                                        )
                                        RuleItem(
                                            icon = Icons.Default.Bolt,
                                            title = "১ নম্বর",
                                            subtitle = "প্রতি সঠিক উত্তরে",
                                            color = Color(0xFF3B82F6),
                                            modifier = Modifier.weight(1f)
                                        )
                                        RuleItem(
                                            icon = Icons.Default.EmojiEvents,
                                            title = "${formatBengaliDigits(arena.questionCount)} পূর্ণমান",
                                            subtitle = "সর্বোচ্চ স্কোর",
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        // Start Test Button
                        item {
                            Button(
                                onClick = { gameState = "COUNTDOWN" },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = arena.primaryColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("live_mock_start_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "মক টেস্ট শুরু করুন",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Top 10 Live Merit List Card
                        item {
                            val currentTop10 = getArenaTop10(arena.id, maxMarks = arena.questionCount)
                            val userScoreInThisArena = arenaUserScoreMap[arena.id]
                            val userRank = userScoreInThisArena?.let { getUserRankInArena(arena.id, it, arena.questionCount) }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, arena.primaryColor.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.EmojiEvents,
                                                contentDescription = null,
                                                tint = ExamGold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "শীর্ষ ১০ লাইভ মেরিট লিস্ট",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = arena.primaryColor.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, arena.primaryColor.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = "মোট নম্বর = ${formatBengaliDigits(arena.questionCount)}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = arena.primaryColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                    // Display 10 students
                                    currentTop10.forEachIndexed { index, userItem ->
                                        val rank = index + 1
                                        val isCurrentUser = userItem.isUser

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isCurrentUser) PrimaryIndigo.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = if (isCurrentUser) BorderStroke(1.5.dp, PrimaryIndigo) else null,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Rank Badge
                                                Box(
                                                    modifier = Modifier.width(32.dp),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    when (rank) {
                                                        1 -> Text("🥇", fontSize = 18.sp)
                                                        2 -> Text("🥈", fontSize = 18.sp)
                                                        3 -> Text("🥉", fontSize = 18.sp)
                                                        else -> Text(
                                                            text = "#${formatBengaliDigits(rank)}",
                                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                // Student Avatar Initial
                                                Surface(
                                                    shape = CircleShape,
                                                    color = userItem.avatarColor.copy(alpha = 0.2f),
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = userItem.name.firstOrNull()?.toString() ?: "U",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                            color = userItem.avatarColor
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                // Student Name and District
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = userItem.name,
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.SemiBold
                                                            ),
                                                            color = if (isCurrentUser) PrimaryIndigo else MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1
                                                        )
                                                        if (isCurrentUser) {
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "(আপনি)",
                                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                color = PrimaryIndigo
                                                            )
                                                        }
                                                    }
                                                    if (userItem.district.isNotBlank()) {
                                                        Text(
                                                            text = userItem.district,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                // Score Badge
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (rank <= 3) ExamGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (rank <= 3) ExamGold else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                                    )
                                                ) {
                                                    Text(
                                                        text = "${formatBengaliDigits(userItem.score)}/${formatBengaliDigits(userItem.maxScore)}",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = if (rank <= 3) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // If user has a score and is ranked outside top 10
                                    if (userScoreInThisArena != null && userRank != null && userRank > 10 && currentTop10.none { it.isUser }) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = PrimaryIndigo.copy(alpha = 0.14f),
                                            border = BorderStroke(1.5.dp, PrimaryIndigo),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "#${formatBengaliDigits(userRank)}",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = PrimaryIndigo,
                                                    modifier = Modifier.width(32.dp)
                                                )
                                                Surface(
                                                    shape = CircleShape,
                                                    color = PrimaryIndigo.copy(alpha = 0.2f),
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text("⭐", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "${studentProfile?.name ?: "আপনার স্কোর"} (আপনি)",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = PrimaryIndigo,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = PrimaryIndigo.copy(alpha = 0.15f),
                                                    border = BorderStroke(1.dp, PrimaryIndigo)
                                                ) {
                                                    Text(
                                                        text = "${formatBengaliDigits(userScoreInThisArena)}/${formatBengaliDigits(arena.questionCount)}",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = PrimaryIndigo,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "COUNTDOWN" -> {
                    // Pulsing Countdown Screen
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryIndigo.copy(alpha = 0.15f),
                                modifier = Modifier.size(140.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isBengali) formatBengaliDigits(countdownSec) else "$countdownSec",
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            fontSize = 64.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = PrimaryIndigo
                                    )
                                }
                            }
                            Text(
                                text = "মক টেস্ট শুরু হতে চলেছে...",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "অল দ্য বেস্ট! মন দিয়ে উত্তর দিন।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                "TEST" -> {
                    // Full-fledged Live Mock Test Environment
                    if (activeTestQuestions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryIndigo)
                        }
                    } else {
                        val currentQuestion = activeTestQuestions.getOrNull(currentQuestionIdx) ?: activeTestQuestions[0]
                        val selectedOption = userSelectedAnswers[currentQuestionIdx]

                        val minutes = testTimeRemainingSec / 60
                        val seconds = testTimeRemainingSec % 60
                        val timeStr = String.format("%02d:%02d", minutes, seconds)
                        val timerColor = when {
                            testTimeRemainingSec <= 120 -> Color(0xFFEF4444) // Red alert < 2 mins
                            testTimeRemainingSec <= 300 -> Color(0xFFF59E0B) // Amber alert < 5 mins
                            else -> PrimaryIndigo
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Top Bar: Timer, Answered counter, and Finish button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Timer Chip (Left)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = timerColor.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, timerColor),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .weight(1f)
                                ) {
                                Row(
                                        modifier = Modifier.padding(horizontal = 1.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = timerColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isBengali) "${formatBengaliDigits(minutes)}:${formatBengaliDigits(seconds)}" else timeStr,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = timerColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(1.dp))

                                // Answered Progress Count (Center)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CorrectGreen.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, CorrectGreen.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .weight(1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${formatBengaliDigits(userSelectedAnswers.size)}/${formatBengaliDigits(totalQuestions)} উত্তর দেওয়া",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = CorrectGreen,
                                            modifier = Modifier.padding(horizontal = 1.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(1.dp))

                                // Quick Submit Button (Right)
                                OutlinedButton(
                                    onClick = { showSubmitConfirmDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                    contentPadding = PaddingValues(horizontal = 1.dp),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = "জমা দিন",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            // Question Palette (Horizontal chip selector 1 to 15)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val listState = rememberLazyListState()
                                LaunchedEffect(currentQuestionIdx) {
                                    listState.animateScrollToItem(
                                        index = currentQuestionIdx,
                                        scrollOffset = -100 // Center adjustment
                                    )
                                }
                                LazyRow(
                                    state = listState,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    items(totalQuestions) { index ->
                                        val isCurrent = index == currentQuestionIdx
                                        val isAnswered = userSelectedAnswers.containsKey(index)
                                        val chipBg = when {
                                            isCurrent -> PrimaryIndigo
                                            isAnswered -> CorrectGreen
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                        val chipTextColor = when {
                                            isCurrent || isAnswered -> Color.White
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = chipBg,
                                            modifier = Modifier
                                                .size(width = 34.dp, height = 30.dp)
                                                .clickable { currentQuestionIdx = index }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = if (isBengali) formatBengaliDigits(index + 1) else "${index + 1}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = chipTextColor
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // Question Navigator Icon
                                var showPaletteDialog by remember { mutableStateOf(false) }
                                IconButton(
                                    onClick = { showPaletteDialog = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.GridView, contentDescription = "Question List", tint = PrimaryIndigo)
                                }

                                if (showPaletteDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showPaletteDialog = false },
                                        title = { Text("প্রশ্ন নির্বাচন করুন") },
                                        text = {
                                            LazyVerticalGrid(
                                                columns = GridCells.Fixed(5),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier
                                                    .padding(top = 8.dp)
                                                    .heightIn(max = 300.dp)
                                            ) {
                                                items(totalQuestions) { index ->
                                                    val isCurrent = index == currentQuestionIdx
                                                    val isAnswered = userSelectedAnswers.containsKey(index)
                                                    val chipBg = when {
                                                        isCurrent -> PrimaryIndigo
                                                        isAnswered -> CorrectGreen
                                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                                    }
                                                    val chipTextColor = when {
                                                        isCurrent || isAnswered -> Color.White
                                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                    }

                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = chipBg,
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clickable {
                                                                currentQuestionIdx = index
                                                                showPaletteDialog = false
                                                            }
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = if (isBengali) formatBengaliDigits(index + 1) else "${index + 1}",
                                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                                color = chipTextColor
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        confirmButton = {
                                            TextButton(onClick = { showPaletteDialog = false }) { Text("Close") }
                                        }
                                    )
                                }
                            }

                            // Question Card (font size 18sp)
                            Card(
                                shape = RoundedCornerShape(2.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .drawBehind {
                                        val s = 2.dp.toPx()
                                        drawLine(cTop, Offset(0f, 0f), Offset(size.width, 0f), s)
                                        drawLine(cRight, Offset(size.width, 0f), Offset(size.width, size.height), s)
                                        drawLine(cBottom, Offset(size.width, size.height), Offset(0f, size.height), s)
                                        drawLine(cLeft, Offset(0f, size.height), Offset(0f, 0f), s)
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = PrimaryIndigo.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "প্রশ্ন নং ${formatBengaliDigits(currentQuestionIdx + 1)}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = PrimaryIndigo,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "১ নম্বর",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    Text(
                                        text = if (isBengali) currentQuestion.questionBengali else currentQuestion.questionEnglish,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            lineHeight = 25.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Options List (ক, খ, গ, ঘ)
                            val options = if (isBengali) currentQuestion.optionsBengali else currentQuestion.optionsEnglish
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                itemsIndexed(options) { index, optionText ->
                                    val isSelected = selectedOption == index

                                    val optionBorder = if (isSelected) BorderStroke(2.dp, PrimaryIndigo) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                                    val optionBg = if (isSelected) PrimaryIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface

                                    val defaultBadgeColor = when (index) {
                                        0 -> Color(0xFF2563EB)
                                        1 -> Color(0xFF059669)
                                        2 -> Color(0xFFD97706)
                                        else -> Color(0xFF7C3AED)
                                    }

                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = optionBg),
                                        border = optionBorder,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                userSelectedAnswers[currentQuestionIdx] = index
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isSelected) PrimaryIndigo else defaultBadgeColor,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    val letter = when (index) {
                                                        0 -> "ক"
                                                        1 -> "খ"
                                                        2 -> "গ"
                                                        else -> "ঘ"
                                                    }
                                                    Text(
                                                        text = letter,
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = Color.White
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Text(
                                                text = optionText,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 15.sp
                                                ),
                                                color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )

                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = PrimaryIndigo,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Bottom Navigation: Previous, Clear, Next
                            BannerAd()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Previous Button
                                OutlinedButton(
                                    onClick = {
                                        if (currentQuestionIdx > 0) currentQuestionIdx--
                                    },
                                    enabled = currentQuestionIdx > 0,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("আগেরটি")
                                }

                                // Clear Answer
                                if (selectedOption != null) {
                                    TextButton(
                                        onClick = { userSelectedAnswers.remove(currentQuestionIdx) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "ক্লিয়ার",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFEF4444)
                                        )
                                    }
                                }

                                // Next Button or Submit
                                if (currentQuestionIdx < totalQuestions - 1) {
                                    Button(
                                        onClick = { currentQuestionIdx++ },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PrimaryIndigo,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                    ) {
                                        Text("পরেরটি")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                                    }
                                } else {
                                    Button(
                                        onClick = { showSubmitConfirmDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CorrectGreen,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .height(50.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("জমা দিন")
                                    }
                                }
                            }
                        }
                    }
                }

                "RESULT" -> {
                    // Final Mock Test Result & Merit List Screen
                    val arena = selectedArena ?: mockArenas[0]
                    val percentage = (correctCount * 100) / totalQuestions
                    val resultTop10 = getArenaTop10(arena.id, correctCount, arena.questionCount)
                    val finalUserRank = getUserRankInArena(arena.id, correctCount, arena.questionCount)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Performance Header
                        item {
                            Surface(
                                shape = CircleShape,
                                color = if (percentage >= 70) ExamGold.copy(alpha = 0.2f) else PrimaryIndigo.copy(alpha = 0.15f),
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (percentage >= 80) "🏆" else if (percentage >= 60) "🎯" else "📝",
                                        fontSize = 38.sp
                                    )
                                }
                            }
                        }

                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when {
                                        percentage >= 80 -> "অসাধারণ পারফরম্যান্স!"
                                        percentage >= 60 -> "চমৎকার প্রস্তুতি!"
                                        percentage >= 40 -> "ভালো চেষ্টা! আরও প্র্যাকটিস করো।"
                                        else -> "আরও রিভিশন প্রয়োজন।"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "${arena.titleBengali} সমাপ্ত হয়েছে",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Stat Summary
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    ResultStatItem(
                                        title = "মোট নম্বর",
                                        value = "${formatBengaliDigits(correctCount)}/${formatBengaliDigits(totalQuestions)}",
                                        color = PrimaryIndigo
                                    )
                                    ResultStatItem(
                                        title = "সঠিক উত্তর",
                                        value = formatBengaliDigits(correctCount),
                                        color = CorrectGreen
                                    )
                                    ResultStatItem(
                                        title = "ভুল উত্তর",
                                        value = formatBengaliDigits(wrongCount),
                                        color = WrongRed
                                    )
                                    ResultStatItem(
                                        title = "পুরস্কার",
                                        value = "+${formatBengaliDigits((correctCount * 15) + 100)} XP",
                                        color = ExamGold
                                    )
                                }
                            }
                        }

                        // Top 10 Live Merit List Card
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, arena.primaryColor.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "🏆 শীর্ষ ১০ লাইভ মেরিট লিস্ট:",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = ExamGold.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "তোমার র‍্যাংক: #${formatBengaliDigits(finalUserRank)}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFFB45309),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                    resultTop10.forEachIndexed { idx, comp ->
                                        val rank = idx + 1
                                        val isCurrentUser = comp.isUser

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isCurrentUser) PrimaryIndigo.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            border = if (isCurrentUser) BorderStroke(1.5.dp, PrimaryIndigo) else null,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.CenterStart) {
                                                    when (rank) {
                                                        1 -> Text("🥇", fontSize = 18.sp)
                                                        2 -> Text("🥈", fontSize = 18.sp)
                                                        3 -> Text("🥉", fontSize = 18.sp)
                                                        else -> Text(
                                                            text = "#${formatBengaliDigits(rank)}",
                                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = if (isCurrentUser) "${comp.name} (আপনি) ⭐" else comp.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal
                                                    ),
                                                    color = if (isCurrentUser) PrimaryIndigo else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.weight(1f)
                                                )

                                                Text(
                                                    text = "${formatBengaliDigits(comp.score)}/${formatBengaliDigits(comp.maxScore)}",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = if (rank <= 3) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }

                                    // If user is outside top 10
                                    if (finalUserRank > 10 && resultTop10.none { it.isUser }) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = PrimaryIndigo.copy(alpha = 0.14f),
                                            border = BorderStroke(1.5.dp, PrimaryIndigo),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "#${formatBengaliDigits(finalUserRank)}",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = PrimaryIndigo,
                                                    modifier = Modifier.width(32.dp)
                                                )
                                                Text(
                                                    text = "${studentProfile?.name ?: "তুমি"} (আপনি) ⭐",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = PrimaryIndigo,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "${formatBengaliDigits(correctCount)}/${formatBengaliDigits(totalQuestions)}",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = PrimaryIndigo
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Detailed Question Solutions & Explanations
                        item {
                            Text(
                                text = "প্রশ্নোত্তর ও বিশদ সমাধান:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        itemsIndexed(activeTestQuestions) { qIdx, question ->
                            val userChoice = userSelectedAnswers[qIdx]
                            val isCorrect = userChoice == question.correctIndex
                            val options = if (isBengali) question.optionsBengali else question.optionsEnglish

                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, if (userChoice == null) MaterialTheme.colorScheme.outlineVariant else if (isCorrect) CorrectGreen.copy(alpha = 0.6f) else WrongRed.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "প্রশ্ন ${formatBengaliDigits(qIdx + 1)}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = PrimaryIndigo
                                        )
                                        Text(
                                            text = when {
                                                userChoice == null -> "উত্তর দেওয়া হয়নি"
                                                isCorrect -> "✓ সঠিক (+১)"
                                                else -> "✗ ভুল (০)"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = when {
                                                userChoice == null -> MaterialTheme.colorScheme.onSurfaceVariant
                                                isCorrect -> CorrectGreen
                                                else -> WrongRed
                                            }
                                        )
                                    }

                                    Text(
                                        text = if (isBengali) question.questionBengali else question.questionEnglish,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Text(
                                        text = if (isBengali) "সঠিক উত্তর: ${options.getOrElse(question.correctIndex) { "" }}" else "Correct: ${options.getOrElse(question.correctIndex) { "" }}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = CorrectGreen
                                    )

                                    if (userChoice != null && !isCorrect) {
                                        Text(
                                            text = if (isBengali) "তোমার উত্তর: ${options.getOrElse(userChoice) { "" }}" else "Your Answer: ${options.getOrElse(userChoice) { "" }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = WrongRed
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "ব্যাখ্যা: ${question.explanationBengali}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Action Buttons: Retake or Lobby
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        gameState = "COUNTDOWN"
                                        hasRewardedBonus = false
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "পুনরায় টেস্ট দিন",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                OutlinedButton(
                                    onClick = { gameState = "LOBBY" },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Text(
                                        text = "অন্যান্য লাইভ মক টেস্ট",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Submit Confirmation Dialog
    if (showSubmitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirmDialog = false },
            title = {
                Text(
                    text = "টেস্ট জমা দিতে চান?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "মোট প্রশ্ন: ${formatBengaliDigits(totalQuestions)}টি"
                    )
                    Text(
                        text = "উত্তর দেওয়া হয়েছে: ${formatBengaliDigits(userSelectedAnswers.size)}টি"
                    )
                    Text(
                        text = "বাকি রয়েছে: ${formatBengaliDigits(unattemptedCount)}টি",
                        color = if (unattemptedCount > 0) Color(0xFFEF4444) else CorrectGreen
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmDialog = false
                        gameState = "RESULT"
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CorrectGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("হ্যাঁ, জমা দিন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSubmitConfirmDialog = false }) {
                    Text("পরীক্ষা চালিয়ে যান")
                }
            }
        )
    }
}

@Composable
private fun RuleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = color,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ResultStatItem(
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
