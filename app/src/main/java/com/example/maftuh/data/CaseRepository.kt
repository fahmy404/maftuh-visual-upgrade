package com.example.maftuh.data

import android.content.Context
import android.content.SharedPreferences

object CaseRepository {

    val CASE_ONE = CaseData(
        id = "case_stopped_clock",
        titleAr = "مقتل رجل أعمال في مدينة نصر",
        subtitleAr = "شقة ٤٢ - الدور الرابع",
        dateAr = "11 أكتوبر - 11:15 مساءً",
        victimNameAr = "أحمد فتحي",
        victimAge = 38,
        briefingAr = "عُثر على المواطن أحمد فتحي (38 سنة) مقتولاً داخل شقته السكنية بمدينة نصر. لا توجد أي آثار كسر على الباب الخارجي، وساعة الحائط بالصالة محطمة، وهاتفه المحمول مفقود، وخزينة غرفة الملابس مفتوحة وخالية. الفريق الجنائي بانتظار توجيهاتك يا حضرة المحقق يوسف.",
        correctCulpritId = "sherif",
        correctMotiveKey = "embezzlement",
        requiredProofIds = listOf("cctv_footage", "phone_in_planter", "tea_cup", "desk_contract"),
        evidenceList = listOf(
            EvidenceItem(
                id = "clock",
                titleAr = "ساعة الحائط المتوقفة",
                titleEn = "Broken Wall Clock",
                category = EvidenceCategory.PHYSICAL,
                location = LocationId.APARTMENT,
                locationDetailAr = "صالة المعيشة - فوق الأريكة",
                descriptionAr = "ساعة حائط كلاسيكية مكسورة الزجاج، العقارب متوقفة بدقة عند 10:45 مساءً.",
                forensicDetailAr = "فحص التروس من الخلف يثبت كسر المسمار الداخلي عمداً بآلة حادة لتثبيت التوقيت عند 10:45، وليس بسبب سقوطها من على الحائط لتضليل المحققين!",
                relatedSuspectId = "sherif",
                isKeyEvidence = true,
                posX = 930f,
                posY = 120f
            ),
            EvidenceItem(
                id = "tea_cup",
                titleAr = "كوب شاي فيه مسحوق غريب",
                titleEn = "Tea Cup with Sedative",
                category = EvidenceCategory.FORENSIC,
                location = LocationId.APARTMENT,
                locationDetailAr = "طاولة طعام المطبخ",
                descriptionAr = "كوب شاي نصف ممتلئ موضوع على طاولة المطبخ، عليه بقايا مسحوق أبيض غريب و3 ملاعق سكر.",
                forensicDetailAr = "التحليل المعملي أظهر مادة مهدئة مركزة ممزوجة بالشاي، وبصمات أصابع تطابق الشريك التجاري شريف محمود.",
                relatedSuspectId = "sherif",
                isKeyEvidence = true,
                posX = 550f,
                posY = 286f
            ),
            EvidenceItem(
                id = "missing_cable",
                titleAr = "وصلة شاحن بدون هاتف",
                titleEn = "Violently Pulled Charger",
                category = EvidenceCategory.DIGITAL,
                location = LocationId.APARTMENT,
                locationDetailAr = "كومودينو غرفة النوم",
                descriptionAr = "وصلة شاحن هاتف حديث موصولة بالكهرباء بجوار السرير لكن الهاتف نفسه انتُزع بعنف وغير موجود.",
                forensicDetailAr = "سحب الكابل السريع ترك علامة شد عنيفة على مقبس الكهرباء، مما يؤكد أن الجاني استولى على هاتف الضحية قبل هروبه لإخفاء المحادثات.",
                relatedSuspectId = "sherif",
                isKeyEvidence = true,
                posX = 840f,
                posY = 230f
            ),
            EvidenceItem(
                id = "desk_contract",
                titleAr = "مستندات مالية وعقد بيع مشبوه",
                titleEn = "Embezzlement Contract",
                category = EvidenceCategory.DOCUMENT,
                location = LocationId.APARTMENT,
                locationDetailAr = "درج غرفة المكتب",
                descriptionAr = "عقد بيع حصة شركة ومذكرة بخط يد أحمد فتحي تهدد شريف بالسجن بتهمة اختلاس 250,000 جنيه.",
                forensicDetailAr = "العقد يحمل توقيعات حديثة وشيكات مرتدة بدون رصيد محررة من شريف لصالح الضحية، مما يمنح شريف دافعاً قوياً لارتكاب الجريمة.",
                relatedSuspectId = "sherif",
                isKeyEvidence = true,
                posX = 945f,
                posY = 560f
            ),
            EvidenceItem(
                id = "damaged_vase",
                titleAr = "مزهرية محطمة قرب المدخل",
                titleEn = "Shattered Porcelain Vase",
                category = EvidenceCategory.PHYSICAL,
                location = LocationId.APARTMENT,
                locationDetailAr = "مدخل الشقة الداخلي",
                descriptionAr = "قطع خزفية مكسورة متناثرة بجانب خزانة الأحذية وباب الشقة الداخلي.",
                forensicDetailAr = "الكسر ناتج عن تصادم جسدي عنيف ودفع متبادل بين شخصين فور فتح الباب، مما ينفي فرضية السقوط العفوي.",
                relatedSuspectId = null,
                isKeyEvidence = false,
                posX = 310f,
                posY = 455f
            ),
            EvidenceItem(
                id = "bank_receipt",
                titleAr = "إيصال سحب بنكي ضخم",
                titleEn = "Bank Withdrawal Receipt",
                category = EvidenceCategory.DOCUMENT,
                location = LocationId.APARTMENT,
                locationDetailAr = "خزانة غرفة الملابس",
                descriptionAr = "إيصال سحب نقدي بمبلغ نصف مليون جنيه تم سحبه من البنك الأهلي قبل الحادثة بيومين.",
                forensicDetailAr = "الخزنة السرية داخل غرفة الملابس وُجدت مفتوحة وخالية تماماً من هذه الأموال، مما يدل على أن القاتل كان يعلم بوجود المبلغ.",
                relatedSuspectId = "sherif",
                isKeyEvidence = true,
                posX = 990f,
                posY = 235f
            ),
            EvidenceItem(
                id = "muddy_footprint",
                titleAr = "أثر حذاء موحل قرب البلكونة",
                titleEn = "Muddy Shoe Print",
                category = EvidenceCategory.FORENSIC,
                location = LocationId.APARTMENT,
                locationDetailAr = "أرضية البلكونة",
                descriptionAr = "طبعة حذاء مقاس 43 عليها بقايا طين رطب من حديقة العمارة باتجاه سور البلكونة.",
                forensicDetailAr = "تطابق نقشة ونعل الحذاء الجلدي الخاص بشريف محمود، مما يثبت محاولته استطلاع الشارع من البلكونة قبل المغادرة.",
                relatedSuspectId = "sherif",
                isKeyEvidence = true,
                posX = 600f,
                posY = 750f
            ),
            EvidenceItem(
                id = "phone_in_planter",
                titleAr = "هاتف الضحية المفقود",
                titleEn = "Victim's Hidden Smartphone",
                category = EvidenceCategory.DIGITAL,
                location = LocationId.STREET,
                locationDetailAr = "حوض زرع مدخل العمارة",
                descriptionAr = "هاتف أحمد فتحي الذكي ملقى بين أوراق شجرة الزينة في مدخل العمارة.",
                forensicDetailAr = "يحتوي على رسالة صوتية مرسلة من شريف محمود في تمام 10:05 مساءً يقول فيها: (أنا طالعلك الشقة حالاً يا أحمد، ومش هسيبك تفضحني وتضيع مستقبلي).",
                relatedSuspectId = "sherif",
                isKeyEvidence = true,
                posX = 420f,
                posY = 530f
            ),
            EvidenceItem(
                id = "cctv_footage",
                titleAr = "تسجيل كاميرا سوبرماركت البركة",
                titleEn = "Supermarket CCTV Tape",
                category = EvidenceCategory.DIGITAL,
                location = LocationId.STREET,
                locationDetailAr = "كشك بقالة البركة المقابل",
                descriptionAr = "شريط فيديو من كاميرا المراقبة الخارجية لسوبرماركت البركة المقابل للعمارة.",
                forensicDetailAr = "يُظهر سيارة شريف السوداء (ميتسوبيشي لانسر) تركن أمام العمارة الساعة 10:12 مساءً، وشريف يخرج مسرعاً ومرتبكاً الساعة 10:38 مساءً!",
                relatedSuspectId = "sherif",
                isKeyEvidence = true,
                posX = 640f,
                posY = 410f
            ),
            EvidenceItem(
                id = "forensic_report",
                titleAr = "تقرير الطبيبة الشرعية د. سلمى",
                titleEn = "Forensic Autopsy Report",
                category = EvidenceCategory.FORENSIC,
                location = LocationId.STATION,
                locationDetailAr = "مكتب الطب الشرعي بالقسم",
                descriptionAr = "التقرير الطبي التشريحي الأولي لجثمان المجني عليه أحمد فتحي.",
                forensicDetailAr = "سبب الوفاة كسر بالجمجمة نتيجة ضربة بجسم صلب ودفع عنيف. توقيت الوفاة البيولوجي المؤكد يقع بين الساعة 10:15 و 10:40 مساءً، مما يكذب توقيت الساعة المتوقفة (10:45)!.",
                relatedSuspectId = null,
                isKeyEvidence = true,
                posX = 280f,
                posY = 220f
            )
        ),
        suspects = listOf(
            Suspect(
                id = "sherif",
                nameAr = "شريف محمود",
                roleAr = "الشريك التجاري للضحية",
                age = 41,
                location = LocationId.STREET,
                posX = 780f,
                posY = 540f,
                avatarEmoji = "💼",
                initialAlibiAr = "أنا قفلت المحل الساعة 10:00 مساءً وروحت على بيتي نمت فوراً، ومشوفتش أحمد من يومين!",
                motiveAr = "خلاف مالي واختلاس 250,000 جنيه وتهديد أحمد بفسخ الشراكة وتقديم بلاغ للنيابة.",
                isGuilty = true,
                dialogueRootNodeId = "sherif_root",
                dialogueNodes = mapOf(
                    "sherif_root" to DialogueNode(
                        id = "sherif_root",
                        suspectLineAr = "خير يا باشا؟ في إيه؟ أنا مصدوم من خبر وفاة أحمد ده كان أعز شريك وصاحب عمري!",
                        options = listOf(
                            DialogueOption(
                                id = "opt_s1",
                                textAr = "كنت فين ليلة الحادثة بين الساعة 10 و 11؟",
                                responseAr = "قلت لحضرتك في المحل بقفل الحسابات، وعلى 10 بالدقيقة خدت عربيتي وروحت بيتي في الهرم ونمت على طول."
                            ),
                            DialogueOption(
                                id = "opt_s2",
                                textAr = "أحمد كان شاكك إن في اختلاسات في فلوس الشراكة بينكم.. إيه ردك؟",
                                responseAr = "كلام فاضي يا فندم! حساباتنا زي الفل وسمن على عسل، مين بس اللي قالك الإشاعات دي؟"
                            ),
                            DialogueOption(
                                id = "opt_s3",
                                textAr = "كاميرا البقالة سجلت عربيتك واقفة قدام عمارة أحمد الساعة 10:12 مساءً!",
                                responseAr = "أنا.. أصل.. أفتكرت إني عديت عليه بسرعة أستسمحه في موضوع الشيك بس مشيت بعد دقيقتين!",
                                requiredEvidenceId = "cctv_footage",
                                breaksAlibi = true,
                                alibiBrokenTextAr = "ارتبك شريف بشدة وتناقضت أقواله حول عدم خروجه من المنزل!"
                            ),
                            DialogueOption(
                                id = "opt_s4",
                                textAr = "لقينا هاتف أحمد في مدخل العمارة وفيه رسالة صوتية منك بتقول (أنا طالعلك الشقة حالاً)!",
                                responseAr = "يا نهار مش فايت.. أنا مكنتش قاصد أقتله يا باشا! هو اللي مسك في رقبتي وهدد يسجنني، زقيته وقع على حرف الطاولة ومات!",
                                requiredEvidenceId = "phone_in_planter",
                                breaksAlibi = true,
                                alibiBrokenTextAr = "انهار شريف محمود واعترف بمواجهة أحمد في نفس توقيت الجريمة وسقوطه قتيلاً!"
                            )
                        )
                    )
                ),
                notesAr = listOf(
                    "يدعي عدم رؤية أحمد ليلة الحادثة.",
                    "لديه نزاع قضائي حول شيك بدون رصيد بقيمة 250 ألف جنيه.",
                    "بصماته وُجدت على كوب الشاي المهدئ بالمطبخ."
                )
            ),
            Suspect(
                id = "dina",
                nameAr = "دينا عادل",
                roleAr = "زوجة الضحية",
                age = 34,
                location = LocationId.STATION,
                posX = 420f,
                posY = 500f,
                avatarEmoji = "👩‍💼",
                initialAlibiAr = "خرجت من الشقة الساعة 8:00 مساءً عشان أزور أختي التعبانة في مصر الجديدة ومارجعتش غير الصبح.",
                motiveAr = "خلافات أسرية متكررة لكنها غير مرتبطة بالحادث الجنائي.",
                isGuilty = false,
                dialogueRootNodeId = "dina_root",
                dialogueNodes = mapOf(
                    "dina_root" to DialogueNode(
                        id = "dina_root",
                        suspectLineAr = "حرام عليكم.. أنا مش قادرة أستوعب اللي حصل لأحمد.. مين اللي ممكن يعمل فيه كده؟",
                        options = listOf(
                            DialogueOption(
                                id = "opt_d1",
                                textAr = "خرجتي من الشقة الساعة كام بالظبط؟ وهل كان أحمد معاه حد؟",
                                responseAr = "خرجت حوالي 8 بالليل، كان لوحده وقالي إنه مستني شريف شريكه عشان ينهوا موضوع الشيك."
                            ),
                            DialogueOption(
                                id = "opt_d2",
                                textAr = "ساعة الحائط في الصالة مكسورة وواقفة عند 10:45.. هل حصل شجار قبل خروجك؟",
                                responseAr = "أبداً والله! الساعة كانت سليمة وشغالة لما نزلت، شكل القاتل حطمها عمداً عشان يورطني في الوقت!"
                            ),
                            DialogueOption(
                                id = "opt_d3",
                                textAr = "معانا شهادة الصيدلية وتذكرة المترو بمصر الجديدة.. أقوالك موثقة تماماً.",
                                responseAr = "الحمد لله يا حضرة المحقق.. دوروا ورا شريف شريكه هو اللي كان بيهدده دايماً بالحبس أو الانتقام."
                            )
                        )
                    )
                ),
                notesAr = listOf(
                    "غادرت الشقة الساعة 8 مساءً وزيارتها لأختها مثبتة بالفواتير والشهود.",
                    "أكدت انتظار زوجها لشريكه شريف لتسوية الشيكات."
                )
            ),
            Suspect(
                id = "amr",
                nameAr = "عمرو سعيد",
                roleAr = "الجار بالشقة المقابلة",
                age = 29,
                location = LocationId.APARTMENT,
                posX = 720f,
                posY = 570f,
                avatarEmoji = "🧔",
                initialAlibiAr = "كنت قاعد في شقتي لابس السماعات بتفرج على فيلم، ومسمعتش أي حاجة خالص.",
                motiveAr = "لا يوجد دافع، مجرد جار متخوف من الإدلاء بالشهادة.",
                isGuilty = false,
                dialogueRootNodeId = "amr_root",
                dialogueNodes = mapOf(
                    "amr_root" to DialogueNode(
                        id = "amr_root",
                        suspectLineAr = "يا فندم أنا في حالي ومليش في المشاكل، أنا راجل مهندس وبحب الهدوء.",
                        options = listOf(
                            DialogueOption(
                                id = "opt_a1",
                                textAr = "مستحيل تكون مسمعتش صوت التكسير والشجار في الشقة اللي قدامك!",
                                responseAr = "بصراحة يا باشا.. أنا خوفت أقول.. أنا سمعت صوت رجل داخل بيزعق لأحمد بعد 10 بالليل وكان صوته تخين وعصبي جداً.",
                                breaksAlibi = true,
                                alibiBrokenTextAr = "كشف الجار عمرو عن سماعه شجاراً حاداً بين رجلين بعد الساعة العاشرة مساءً!"
                            ),
                            DialogueOption(
                                id = "opt_a2",
                                textAr = "هل شفت شكل الشخص ده وهو طالع أو نازل من العمارة؟",
                                responseAr = "بصيت من العين السحرية لما سمعت الباب بيترزع.. شفت راجل ببدلة كحلي وشنطة سودا نازل بيجري على السلم ومرتبك جداً."
                            )
                        )
                    )
                ),
                notesAr = listOf(
                    "سمع شجاراً عنيفاً بعد العاشرة مساءً.",
                    "رأى رجلاً ببدلة كحلي (تطابق ملابس شريف) يفر مهرولاً على الدرج."
                )
            ),
            Suspect(
                id = "hossam",
                nameAr = "حسام فتحي",
                roleAr = "شقيق الضحية الأصغر",
                age = 31,
                location = LocationId.STATION,
                posX = 880f,
                posY = 360f,
                avatarEmoji = "🧑‍🦱",
                initialAlibiAr = "كنت مسافر إسكندرية بقالي أسبوع ومعرفتش بالحادثة غير لما القسم اتصل بيا.",
                motiveAr = "خلاف قديم حول تقسيم ميراث شقة الوالد تم حله ودياً.",
                isGuilty = false,
                dialogueRootNodeId = "hossam_root",
                dialogueNodes = mapOf(
                    "hossam_root" to DialogueNode(
                        id = "hossam_root",
                        suspectLineAr = "أحمد أخويا الكبير وسندي في الدنيا.. أنا متأكد إن شريكه شريف هو السبب!",
                        options = listOf(
                            DialogueOption(
                                id = "opt_h1",
                                textAr = "كنت فين ليلة الحادثة يا حسام؟",
                                responseAr = "كنت في شغلي بالإسكندرية وتذكرة القطر وكارت البنزين يثبتوا إني وصلت القاهرة الصبح بدري بعد الاتصال."
                            ),
                            DialogueOption(
                                id = "opt_h2",
                                textAr = "هل أحمد حكالك عن مشاكله مع شريف في الفترة الأخيرة؟",
                                responseAr = "أيوه، قالي إن شريف سرق شيكات الشركة واختلس 250 ألف، وإنه هيوديه في داهية لو مرجعش الفلوس في نفس اليوم."
                            )
                        )
                    )
                ),
                notesAr = listOf(
                    "تواجد بالإسكندرية مثبت بوثائق السفر الرسمية وتذاكر القطار.",
                    "يشهد على التهديدات المتبادلة بين شريف وشقيقه."
                )
            )
        ),
        perfectEnding = CaseEnding(
            titleAr = "اعتراف كامل وإدانة قاطعة بالجناية",
            descriptionAr = "نجحت باقتدار يا حضرة المحقق يوسف في كشف خطة شريف محمود المحكمة؛ حيث حطم الساعة عمداً لتضليل توقيت الجريمة، لكن تسجيل كاميرا سوبرماركت البركة وهاتف الضحية ورسالة التهديد الصوتي وفحص كوب الشاي المخدر كشفوا كذبته بالكامل. اعترف بالجريمة وتمت إحالته لمحكمة الجنايات مع مصادرة الأموال المختلسة.",
            ratingAr = "رتبة: محقق ممتاز ★★★ (امتياز)",
            stars = 3,
            score = 1000
        ),
        partialEnding = CaseEnding(
            titleAr = "توجيه اتهام دون أدلة كافية للحبس",
            descriptionAr = "اشتبهت في الشخص الصحيح (شريف محمود) لكنك لم تقدم كافة الأدلة الجنائية الدامغة مثل تسجيل الكاميرا أو هاتف الضحية، مما سمح لمحاميه بالطعن في تقرير الضبط وإخلاء سبيله بكفالة مالية على ذمة التحقيق مؤقتاً.",
            ratingAr = "رتبة: محقق مبتدئ ★★ (جيد)",
            stars = 2,
            score = 500
        ),
        wrongEnding = CaseEnding(
            titleAr = "اتهام باطل وإفلات الجاني الحقيقي",
            descriptionAr = "اتهمت شخصاً بريئاً استناداً إلى أدلة ناقصة أو خدعة ساعة الحائط المفبركة، مما سمح للجاني الحقيقي (شريف محمود) بحزم حقائبه والهروب خارج البلاد بجواز سفر مزور، وقُيدت القضية ضد مجهول.",
            ratingAr = "رتبة: إخفاق في التحقيق ★ (ضعيف)",
            stars = 1,
            score = 100
        )
    )

    private const val PREFS_NAME = "maftuh_detective_save"
    private const val KEY_DISCOVERED = "discovered_evidence"
    private const val KEY_INTERROGATED = "interrogated_suspects"
    private const val KEY_BROKEN = "broken_alibis"
    private const val KEY_LOCATION = "current_location"
    private const val KEY_SCORE = "score"

    fun saveProgress(context: Context, progress: CaseProgress) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putStringSet(KEY_DISCOVERED, progress.discoveredEvidenceIds)
            putStringSet(KEY_INTERROGATED, progress.interrogatedSuspectIds)
            putStringSet(KEY_BROKEN, progress.brokenAlibiSuspectIds)
            putString(KEY_LOCATION, progress.currentLocation.name)
            putInt(KEY_SCORE, progress.score)
            apply()
        }
    }

    fun loadProgress(context: Context): CaseProgress {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val discovered = prefs.getStringSet(KEY_DISCOVERED, emptySet()) ?: emptySet()
        val interrogated = prefs.getStringSet(KEY_INTERROGATED, emptySet()) ?: emptySet()
        val broken = prefs.getStringSet(KEY_BROKEN, emptySet()) ?: emptySet()
        val locName = prefs.getString(KEY_LOCATION, LocationId.APARTMENT.name)
        val location = try {
            LocationId.valueOf(locName ?: LocationId.APARTMENT.name)
        } catch (_: Exception) {
            LocationId.APARTMENT
        }
        val score = prefs.getInt(KEY_SCORE, discovered.size * 50 + broken.size * 100)

        return CaseProgress(
            discoveredEvidenceIds = discovered,
            interrogatedSuspectIds = interrogated,
            brokenAlibiSuspectIds = broken,
            currentLocation = location,
            score = score
        )
    }

    fun clearProgress(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
