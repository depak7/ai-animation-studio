from manim import *

class ArchitectureDiagram(Scene):
    def construct(self):
        # Define objects
        backend = Rectangle(width=2, height=1.5, color=BLUE, fill_opacity=0.5).shift(LEFT * 3).set_z_index(1)
        backend_text = Text("Backend", font_size=24).move_to(backend.get_center())
        backend_group = Group(backend, backend_text)

        redis = Rectangle(width=2, height=1.5, color=GREEN, fill_opacity=0.5).shift(RIGHT * 3 + UP*1.5)
        redis_text = Text("Redis", font_size=24).move_to(redis.get_center())
        redis_group = Group(redis, redis_text)

        database = Rectangle(width=2, height=1.5, color=RED, fill_opacity=0.5).shift(RIGHT * 3 + DOWN*1.5)
        database_text = Text("Database", font_size=24).move_to(database.get_center())
        database_group = Group(database, database_text)

        db_arrow = Arrow(backend.get_right(), database.get_left(), buff=0.5)
        redis_arrow = Arrow(backend.get_right(), redis.get_left(), buff=0.5)
        redis_to_backend_arrow = Arrow(redis.get_right(), backend.get_top(), buff=0.5)

        db_time = Text("100ms", font_size=20).next_to(db_arrow, DOWN)
        redis_time = Text("10ms", font_size=20).next_to(redis_arrow, UP)

        db_query_text = Text("Database Query", font_size=20).next_to(db_arrow, UP)
        redis_query_text = Text("Redis Query", font_size=20).next_to(redis_arrow, DOWN)
        data_text = Text("Data", font_size=20).next_to(redis_to_backend_arrow, RIGHT)


        # Animate
        self.play(Create(backend_group))
        self.play(Create(database_group))
        self.play(Create(redis_group))

        self.play(Create(db_arrow))
        self.play(Write(db_query_text))
        self.play(Write(db_time))
        self.wait(0.5)

        self.play(Create(redis_arrow))
        self.play(Write(redis_query_text))
        self.play(Write(redis_time))

        self.play(Create(redis_to_backend_arrow))
        self.play(Write(data_text))


        self.wait(3)
