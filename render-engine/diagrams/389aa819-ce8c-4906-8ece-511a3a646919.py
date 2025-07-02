from manim import *

class ArchitectureDiagram(Scene):
    def construct(self):
        backend = Rectangle(width=2, height=2).shift(LEFT * 4).set_fill(BLUE, opacity=0.5).set_stroke(BLUE_E, width=3)
        backend_text = Text("Backend").scale(0.7).move_to(backend.get_center())
        self.play(Create(backend), Write(backend_text))

        redis = Rectangle(width=2, height=2).shift(UP * 2).set_fill(GREEN, opacity=0.5).set_stroke(GREEN_E, width=3)
        redis_text = Text("Redis").scale(0.7).move_to(redis.get_center())
        self.play(Create(redis), Write(redis_text))

        database = Rectangle(width=2, height=2).shift(DOWN * 2).set_fill(RED, opacity=0.5).set_stroke(RED_E, width=3)
        database_text = Text("Database").scale(0.7).move_to(database.get_center())
        self.play(Create(database), Write(database_text))

        db_query = Arrow(backend.get_right(), database.get_left(), buff=0.5, color=WHITE)
        db_query_text = Text("Query").scale(0.6).move_to(backend.get_right() + LEFT + DOWN*1)

        redis_query = Arrow(backend.get_right(), redis.get_left(), buff=0.5, color=WHITE)
        redis_query_text = Text("Query").scale(0.6).move_to(backend.get_right() + LEFT + UP*1)

        self.play(Create(db_query), Write(db_query_text))
        self.play(Create(redis_query), Write(redis_query_text))

        db_time = Text("100ms").scale(0.7).move_to(RIGHT * 3 + DOWN * 2)
        redis_time = Text("1ms").scale(0.7).move_to(RIGHT * 3 + UP * 2)

        self.play(Write(db_time))
        self.play(Write(redis_time))

        self.wait(2)
