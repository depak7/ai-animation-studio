from manim import *

class ArchitectureDiagram(Scene):
    def construct(self):
        backend = Rectangle(width=2, height=1).shift(LEFT * 4).shift(UP)
        backend_text = Text('Backend').move_to(backend)

        database = Rectangle(width=2, height=1).shift(RIGHT * 4).shift(DOWN)
        database_text = Text('Database').move_to(database)

        redis = Rectangle(width=2, height=1).shift(RIGHT * 4).shift(UP)
        redis_text = Text('Redis').move_to(redis)

        arrow_db = Arrow(backend.get_edge(DOWN), database.get_edge(UP), buff=0.5)
        arrow_db_text = Text('DB Query').move_to(arrow_db.get_center()).shift(UP * 0.5)

        arrow_redis = Arrow(backend.get_edge(UP), redis.get_edge(DOWN), buff=0.5)
        arrow_redis_text = Text('Redis Query').move_to(arrow_redis.get_center()).shift(DOWN * 0.5)

        time_db = Text('500ms').shift(DOWN)
        time_redis = Text('5ms').shift(UP)

        self.play(Create(backend), Write(backend_text))
        self.play(Create(database), Write(database_text))
        self.play(Create(redis), Write(redis_text))
        self.play(Create(arrow_db), Write(arrow_db_text))
        self.play(Create(arrow_redis), Write(arrow_redis_text))
        self.play(Write(time_db))
        self.play(Write(time_redis))

        self.wait(2)
