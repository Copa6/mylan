from django.db import models

# Create your models here.


class Document(models.Model):
    docfile = models.FileField(upload_to='skin_cancer/')

    def __str__(self):
        return self.docfile.name


class SelectedFile(models.Model):
    filename = models.CharField(max_length=100)

    def __str__(self):
        return self.filename